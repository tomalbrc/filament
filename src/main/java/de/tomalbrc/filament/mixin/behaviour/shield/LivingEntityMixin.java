package de.tomalbrc.filament.mixin.behaviour.shield;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract ItemStack getUseItem();

    @Inject(method = "isBlocking", at = @At(value = "RETURN"), cancellable = true)
    private void filament$customShieldBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.getUseItem().getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.SHIELD)) {
            cir.setReturnValue(true);
        }
    }
}
