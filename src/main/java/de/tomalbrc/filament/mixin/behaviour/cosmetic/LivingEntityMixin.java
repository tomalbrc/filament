package de.tomalbrc.filament.mixin.behaviour.cosmetic;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.cosmetic.AnimatedCosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.cosmetic.CosmeticUtil;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.registry.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin implements CosmeticInterface {
    @Unique
    private final IntArraySet displays = new IntArraySet();

    @Unique
    private final Map<String, ElementHolder> filamentCosmeticHolder = new Object2ObjectOpenHashMap<>();

    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        Cosmetic.Config cosmetic = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmetic != null) {
            cir.setReturnValue(cosmetic.slot);
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD"))
    private void filament$customOnEquipItem(EquipmentSlot equipmentSlot, ItemStack oldItemStack, ItemStack newItemStack, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            return;
        }

        if (oldItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(oldItemStack)) {
            var component = oldItemStack.get(DataComponents.EQUIPPABLE);
            var slot = component == null ? simpleItem.get(Behaviours.COSMETIC).getConfig().slot : component.slot();
            if (slot == equipmentSlot) filament$destroyHolder(slot.getName());
        }

        if (newItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(newItemStack)) {
            var component = newItemStack.get(DataComponents.EQUIPPABLE);
            var slot = component == null ? simpleItem.get(Behaviours.COSMETIC).getConfig().slot : component.slot();
            if (slot == equipmentSlot) {
                filament$destroyHolder(slot.getName());
                filament$addHolder(LivingEntity.class.cast(this), newItemStack.getItem(), newItemStack, slot.getName());
            }
        }
    }

    @Inject(method = "doHurtEquipment", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filament$customOnDoHurtEquipment(DamageSource damageSource, float f, EquipmentSlot[] equipmentSlots, CallbackInfo ci, @Local ItemStack itemStack, @Local EquipmentSlot equipmentSlot) {
        if (!(itemStack.getItem() instanceof ArmorItem) && itemStack.getItem() instanceof SimpleItem && itemStack.canBeHurtBy(damageSource)) {
            int i = (int)Math.max(1.0F, f / 4.0F);
            itemStack.hurtAndBreak(i, (LivingEntity)(Object)this, equipmentSlot);
        }
    }

    @Inject(method = "remove", at = @At(value = "HEAD"))
    private void filament$onRemove(Entity.RemovalReason removalReason, CallbackInfo ci) {
        var self = LivingEntity.class.cast(this);
        for (ItemStack itemStack : self.getArmorSlots()) {
            if (CosmeticUtil.isCosmetic(itemStack))
                filament$destroyHolder(self.getEquipmentSlotForItem(itemStack).getName());
        }
    }

    @Unique
    @Override
    public void filament$addHolder(LivingEntity livingEntity, Item simpleItem, ItemStack itemStack, String slot) {
        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(simpleItem);

        if (cosmeticData.model != null && !filamentCosmeticHolder.containsKey(slot)) {
            Consumer<ServerGamePacketListenerImpl> cb = (player) -> {
                player.send(VirtualEntityUtils.createRidePacket(livingEntity.getId(), this.displays.toIntArray()));
            };
            var animatedCosmeticHolder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model), cb);
            EntityAttachment.ofTicking(animatedCosmeticHolder, livingEntity);

            for (VirtualElement element : animatedCosmeticHolder.getElements()) {
                displays.addAll(element.getEntityIds());
            }

            if (livingEntity instanceof ServerPlayer serverPlayer)
                animatedCosmeticHolder.startWatching(serverPlayer);

            if (cosmeticData.autoplay != null) {
                animatedCosmeticHolder.getAnimator().playAnimation(cosmeticData.autoplay);
            }

            filamentCosmeticHolder.put(slot, animatedCosmeticHolder);
        }
        else if (!filamentCosmeticHolder.containsKey(slot)) {
            Consumer<ServerGamePacketListenerImpl> cb = (player) -> {
                player.send(VirtualEntityUtils.createRidePacket(livingEntity.getId(), this.displays.toIntArray()));
            };
            var cosmeticHolder = new CosmeticHolder(livingEntity, itemStack, cb);
            EntityAttachment.ofTicking(cosmeticHolder, livingEntity);

            for (VirtualElement element : cosmeticHolder.getElements()) {
                displays.addAll(element.getEntityIds());
            }

            if (livingEntity instanceof ServerPlayer serverPlayer)
                cosmeticHolder.startWatching(serverPlayer);

            filamentCosmeticHolder.put(slot, cosmeticHolder);
        }
    }

    @Unique
    @Override
    public void filament$destroyHolder(String slot) {
        if (filamentCosmeticHolder.containsKey(slot)) {
            var holder = filamentCosmeticHolder.get(slot);

            for (VirtualElement element : holder.getElements()) {
                displays.removeAll(element.getEntityIds());
            }

            filamentCosmeticHolder.get(slot).getAttachment().destroy();
            filamentCosmeticHolder.get(slot).destroy();
            filamentCosmeticHolder.remove(slot);
        }
    }
}
