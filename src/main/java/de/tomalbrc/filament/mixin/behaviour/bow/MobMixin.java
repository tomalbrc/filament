package de.tomalbrc.filament.mixin.behaviour.bow;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Shadow public abstract boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2);

    @Inject(method = "canReplaceCurrentItem", at = @At("HEAD"), cancellable = true)
    private void filament$onCanReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if (filament$isBow(itemStack) && filament$isBow(itemStack2)) {
            cir.setReturnValue(this.canReplaceEqualItem(itemStack, itemStack2));
        }
    }

    @Unique
    private boolean filament$isBow(ItemStack itemStack) {
        return itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.BOW) || itemStack.getItem() instanceof BowItem;
    }
}
