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
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.impl.transfer.item.CursorSlotWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements CosmeticInterface {
    @Shadow public abstract void onItemPickup(ItemEntity itemEntity);

    @Shadow protected abstract void serverAiStep();

    @Shadow public abstract void remove(RemovalReason removalReason);

    @Unique
    private final Map<String, CosmeticHolder> filamentCosmeticHolder = new Object2ObjectOpenHashMap<>();

    @Unique
    private final Map<String, AnimatedCosmeticHolder> filamentAnimatedCosmeticHolder = new Object2ObjectOpenHashMap<>();

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        Cosmetic.Config cosmetic = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmetic != null && cosmetic.slot != null) {
            cir.setReturnValue(cosmetic.slot);
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customOnEquipItem(EquipmentSlot equipmentSlot, ItemStack oldItemStack, ItemStack newItemStack, CallbackInfo ci) {
        if (oldItemStack.getItem() == newItemStack.getItem()) {
            return;
        }

        if (oldItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(oldItemStack)) {
            var component = oldItemStack.get(DataComponents.EQUIPPABLE);
            var cosmetic = simpleItem.get(Behaviours.COSMETIC);

            var newIsArmor = newItemStack.getItem() instanceof ArmorItem;
            var canMask = cosmetic != null && cosmetic.getConfig().mask;
            if (canMask && oldItemStack.has(DataComponents.CUSTOM_DATA)) { // pop item or replace data
                if (LivingEntity.class.cast(this) instanceof ServerPlayer serverPlayer) {
                    var item = Cosmetic.getOriginal(this.level().registryAccess(), oldItemStack);
                    System.out.println("OUT getOrig");
                    if (!newIsArmor) { // pop
                        oldItemStack.remove(DataComponents.CUSTOM_DATA);
                        serverPlayer.addItem(item);
                    } else { // replace data
                        //Cosmetic.updateMaskedData(this.level().registryAccess(), newItemStack, oldItemStack);
                        //serverPlayer.inventoryMenu.setCarried(item);
                    }
                }
            }

            if (canMask && newItemStack.getItem() instanceof ArmorItem) {
                Cosmetic.updateMaskedData(this.level().registryAccess(), newItemStack, oldItemStack);
                System.out.println("OUT update insert item with armor as cursor");
                if (LivingEntity.class.cast(this) instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                    serverPlayer.inventoryMenu.setCarried(oldItemStack.copy());
                    oldItemStack.setCount(0);
                }
                newItemStack.setCount(0);
            }

            var slot = component == null ? simpleItem.get(Behaviours.COSMETIC).getConfig().slot : component.slot();
            if (slot == equipmentSlot && equipmentSlot != EquipmentSlot.HEAD) filament$destroyHolder(slot.getName());
        }

        if (newItemStack.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(newItemStack)) {
            var component = newItemStack.get(DataComponents.EQUIPPABLE);
            var cosmetic = simpleItem.get(Behaviours.COSMETIC);

            if (cosmetic != null && cosmetic.getConfig().mask && oldItemStack.getItem() instanceof ArmorItem) {
                System.out.println("OUT update insert item with cosmetic as cursor");
                Cosmetic.updateMaskedData(this.level().registryAccess(), oldItemStack, newItemStack);
                oldItemStack.setCount(0);
            }

            var slot = component == null ? cosmetic.getConfig().slot : component.slot();
            if (slot == equipmentSlot && equipmentSlot != EquipmentSlot.HEAD) {
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

        if (cosmeticData.model != null) {
            if (!filamentAnimatedCosmeticHolder.containsKey(slot)) {
                var animatedCosmeticHolder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model));
                EntityAttachment.ofTicking(animatedCosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    animatedCosmeticHolder.startWatching(serverPlayer);

                if (cosmeticData.autoplay != null) {
                    animatedCosmeticHolder.getAnimator().playAnimation(cosmeticData.autoplay);
                }
                filamentAnimatedCosmeticHolder.put(slot, animatedCosmeticHolder);
            }
        }
        else {
            if (!filamentCosmeticHolder.containsKey(slot)) {
                var cosmeticHolder = new CosmeticHolder(livingEntity, itemStack);
                EntityAttachment.ofTicking(cosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    cosmeticHolder.startWatching(serverPlayer);

                filamentCosmeticHolder.put(slot, cosmeticHolder);
            }
        }
    }

    @Unique
    @Override
    public void filament$destroyHolder(String slot) {
        if (filamentAnimatedCosmeticHolder.containsKey(slot)) {
            filamentAnimatedCosmeticHolder.get(slot).getAttachment().destroy();
            filamentAnimatedCosmeticHolder.get(slot).destroy();
            filamentAnimatedCosmeticHolder.remove(slot);
        }
        if (filamentCosmeticHolder.containsKey(slot)) {
            filamentCosmeticHolder.get(slot).getAttachment().destroy();
            filamentCosmeticHolder.get(slot).destroy();
            filamentCosmeticHolder.remove(slot);
        }
    }
}
