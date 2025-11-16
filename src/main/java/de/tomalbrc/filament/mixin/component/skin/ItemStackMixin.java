package de.tomalbrc.filament.mixin.component.skin;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean isDamageableItem();

    @Shadow public abstract int getDamageValue();

    @Shadow public abstract int getMaxDamage();

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At(value = "TAIL"))
    private void filament$dontDestroySkin(int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        var self = ItemStack.class.cast(this);
        var b = this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage();
        if (b && self.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var skinItem = self.get(FilamentComponents.SKIN_DATA_COMPONENT);
            if (skinItem != null && !skinItem.isEmpty()) {
                self.remove(FilamentComponents.SKIN_DATA_COMPONENT);

                if (livingEntity instanceof Player serverPlayer) {
                    if (!serverPlayer.addItem(skinItem))
                        serverPlayer.spawnAtLocation(skinItem);
                } else {
                    livingEntity.spawnAtLocation(skinItem);
                }

            }
        }
    }


}
