package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getEquipmentSlotForItem", at = @At(value = "HEAD"), cancellable = true)
    private void filament$customGetEquipmentSlotForItem(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem) {
            if (simpleItem.getItemData().isArmor() || simpleItem.getItemData().isCosmetic()) {
                var slot = simpleItem.getItemData().isArmor() ?
                        simpleItem.getItemData().behaviour().armor.slot:
                        simpleItem.getItemData().behaviour().cosmetic.slot;
                cir.setReturnValue(slot);
            }
        }
    }
}
