package de.tomalbrc.filament.mixin.cosmetic;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Armor;
import de.tomalbrc.filament.behaviour.item.Cosmetic;
import de.tomalbrc.filament.cosmetic.AnimatedCosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticHolder;
import de.tomalbrc.filament.cosmetic.CosmeticInterface;
import de.tomalbrc.filament.cosmetic.CosmeticUtil;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.registry.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;
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

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin implements CosmeticInterface {
    @Unique
    private CosmeticHolder filamentCosmeticHolder;

    @Unique
    private AnimatedCosmeticHolder filamentAnimatedCosmeticHolder;

    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        Cosmetic.Config cosmetic = CosmeticUtil.getCosmeticData(itemStack);
        if (cosmetic != null) {
            cir.setReturnValue(cosmetic.slot);
        }
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.ARMOR))  {
            Armor.Config armor = simpleItem.get(Behaviours.ARMOR).getConfig();
            cir.setReturnValue(armor.slot);
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD"))
    private void filament$customOnEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.CHEST && (filamentCosmeticHolder != null || filamentAnimatedCosmeticHolder != null)) {
            filament$destroyHolder();
        }

        if (equipmentSlot == EquipmentSlot.CHEST && !itemStack2.isEmpty() && CosmeticUtil.isCosmetic(itemStack2) && (Object)this instanceof LivingEntity serverPlayer) {
            filament$addHolder(serverPlayer, itemStack2.getItem(), itemStack2);
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
        filament$destroyHolder();
    }

    @Unique public void filament$addHolder(LivingEntity livingEntity, Item simpleItem, ItemStack itemStack) {
        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(simpleItem);

        if (cosmeticData.model != null) {
            if (filamentAnimatedCosmeticHolder == null) {
                filamentAnimatedCosmeticHolder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model));
                EntityAttachment.ofTicking(filamentAnimatedCosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    filamentAnimatedCosmeticHolder.startWatching(serverPlayer);

                if (cosmeticData.autoplay != null) {
                    filamentAnimatedCosmeticHolder.getAnimator().playAnimation(cosmeticData.autoplay);
                }
            }
        }
        else {
            if (filamentCosmeticHolder == null) {
                filamentCosmeticHolder = new CosmeticHolder(livingEntity, itemStack);
                EntityAttachment.ofTicking(filamentCosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    filamentCosmeticHolder.startWatching(serverPlayer);
            }
        }
    }

    @Unique
    public void filament$destroyHolder() {
        if (filamentAnimatedCosmeticHolder != null) {
            filamentAnimatedCosmeticHolder.getAttachment().destroy();
            filamentAnimatedCosmeticHolder.destroy();
            filamentAnimatedCosmeticHolder = null;
        }
        if (filamentCosmeticHolder != null) {
            filamentCosmeticHolder.getAttachment().destroy();
            filamentCosmeticHolder.destroy();
            filamentCosmeticHolder = null;
        }
    }
}
