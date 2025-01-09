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
import org.spongepowered.asm.mixin.Shadow;
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
    @Shadow public abstract EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack);

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

    @Unique
    private final IntArraySet displays = new IntArraySet();

    @Unique
    private final Map<String, ElementHolder> filamentCosmeticHolder = new Object2ObjectOpenHashMap<>();

    // COSMETIC, ARMOR, ELYTRA
    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.getEquipmentSlot() != EquipmentSlot.MAINHAND)  {
            cir.setReturnValue(simpleItem.getEquipmentSlot());
        }
    }

    // ARMOR
    @Inject(method = "onEquipItem", at = @At(value = "HEAD"))
    private void filament$customOnEquipItem(EquipmentSlot equipmentSlot, ItemStack oldItemStack, ItemStack newItemStack, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            return;
        }

        if (oldItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(oldItemStack)) {
            var slot = simpleItem.get(Behaviours.COSMETIC).getConfig().slot;
            if (slot == equipmentSlot) filament$destroyHolder(slot.getName());
        }

        // hotswap case
        {
            if (oldItemStack.isEmpty() && !CosmeticUtil.isCosmetic(this.getItemBySlot(this.getEquipmentSlotForItem(newItemStack)))) {
                var slot = this.getEquipmentSlotForItem(newItemStack);
                filament$destroyHolder(slot.getName());
            }
        }

        if (newItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(newItemStack)) {
            var slot = simpleItem.get(Behaviours.COSMETIC).getConfig().slot;
            if (slot == equipmentSlot || (oldItemStack.isEmpty() && equipmentSlot != EquipmentSlot.MAINHAND)) {
                filament$destroyHolder(slot.getName());
                filament$addHolder(LivingEntity.class.cast(this), newItemStack.getItem(), newItemStack, slot.getName());
            }
        }
    }

    // ARMOR
    @Inject(method = "doHurtEquipment", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filament$customOnDoHurtEquipment(DamageSource damageSource, float f, EquipmentSlot[] equipmentSlots, CallbackInfo ci, @Local ItemStack itemStack, @Local EquipmentSlot equipmentSlot) {
        if (!(itemStack.getItem() instanceof ArmorItem) && itemStack.getItem() instanceof SimpleItem && itemStack.canBeHurtBy(damageSource)) {
            int i = (int)Math.max(1.0F, f / 4.0F);
            itemStack.hurtAndBreak(i, (LivingEntity)(Object)this, equipmentSlot);
        }
    }

    // COSMETIC
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

        ElementHolder holder = null;
        Consumer<ServerGamePacketListenerImpl> cb = (player) -> {
            player.send(VirtualEntityUtils.createRidePacket(livingEntity.getId(), this.displays.toIntArray()));
        };

        if (cosmeticData.model != null && !filamentCosmeticHolder.containsKey(slot)) {
            holder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model), cb);
        }
        else if (!filamentCosmeticHolder.containsKey(slot)) {
            holder = new CosmeticHolder(livingEntity, itemStack, cb);
        }

        EntityAttachment.ofTicking(holder, livingEntity);

        for (VirtualElement element : holder.getElements()) {
            displays.addAll(element.getEntityIds());
        }

        if (livingEntity instanceof ServerPlayer serverPlayer)
            holder.startWatching(serverPlayer);

        if (cosmeticData.autoplay != null && holder instanceof AnimatedCosmeticHolder animatedHolder) {
            animatedHolder.getAnimator().playAnimation(cosmeticData.autoplay);
        }

        filamentCosmeticHolder.put(slot, holder);
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
