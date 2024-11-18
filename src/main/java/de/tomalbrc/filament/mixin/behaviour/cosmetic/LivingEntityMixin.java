package de.tomalbrc.filament.mixin.behaviour.cosmetic;

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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

import java.util.Map;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin implements CosmeticInterface {
    @Unique
    private final Map<ItemStack, CosmeticHolder> filamentCosmeticHolder = new Object2ObjectOpenHashMap<>();

    @Unique
    private final Map<ItemStack, AnimatedCosmeticHolder> filamentAnimatedCosmeticHolder = new Object2ObjectOpenHashMap<>();

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
        if (CosmeticUtil.isCosmetic(itemStack)) {
            filament$destroyHolder(itemStack);
        }

        if (itemStack2.getItem() instanceof SimpleItem simpleItem && CosmeticUtil.isCosmetic(itemStack2) && simpleItem.get(Behaviours.COSMETIC).getConfig().slot == equipmentSlot) {
            filament$addHolder(LivingEntity.class.cast(this), itemStack2.getItem(), itemStack2);
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
        for (ItemStack itemStack : LivingEntity.class.cast(this).getArmorSlots()) {
            if (CosmeticUtil.isCosmetic(itemStack))
                filament$destroyHolder(itemStack);
        }
    }

    @Unique
    @Override
    public void filament$addHolder(LivingEntity livingEntity, Item simpleItem, ItemStack itemStack) {
        Cosmetic.Config cosmeticData = CosmeticUtil.getCosmeticData(simpleItem);

        if (cosmeticData.model != null) {
            if (!filamentAnimatedCosmeticHolder.containsKey(itemStack)) {
                var animatedCosmeticHolder = new AnimatedCosmeticHolder(livingEntity, ModelRegistry.getModel(cosmeticData.model));
                EntityAttachment.ofTicking(animatedCosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    animatedCosmeticHolder.startWatching(serverPlayer);

                if (cosmeticData.autoplay != null) {
                    animatedCosmeticHolder.getAnimator().playAnimation(cosmeticData.autoplay);
                }
                filamentAnimatedCosmeticHolder.put(itemStack, animatedCosmeticHolder);
            }
        }
        else {
            if (!filamentCosmeticHolder.containsKey(itemStack)) {
                var cosmeticHolder = new CosmeticHolder(livingEntity, itemStack);
                EntityAttachment.ofTicking(cosmeticHolder, livingEntity);

                if (livingEntity instanceof ServerPlayer serverPlayer)
                    cosmeticHolder.startWatching(serverPlayer);

                filamentCosmeticHolder.put(itemStack, cosmeticHolder);
            }
        }
    }

    @Unique
    @Override
    public void filament$destroyHolder(ItemStack itemStack) {
        if (filamentAnimatedCosmeticHolder.containsKey(itemStack)) {
            filamentAnimatedCosmeticHolder.get(itemStack).getAttachment().destroy();
            filamentAnimatedCosmeticHolder.get(itemStack).destroy();
            filamentAnimatedCosmeticHolder.remove(itemStack);
        }
        if (filamentCosmeticHolder.containsKey(itemStack)) {
            filamentCosmeticHolder.get(itemStack).getAttachment().destroy();
            filamentCosmeticHolder.get(itemStack).destroy();
            filamentCosmeticHolder.remove(itemStack);
        }
    }
}
