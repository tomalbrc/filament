package de.tomalbrc.filament.mixin.behaviour;

import de.tomalbrc.filament.registry.FuelRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fuel behaviourConfig support
@Mixin(AbstractFurnaceBlockEntity.class)
public class FuelValuesMixin {
    @Inject(method = "isFuel", at = @At("RETURN"), cancellable = true)
    private static void filament$isCustomFuel(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (FuelRegistry.getCache().containsKey(itemStack.getItem()))
            cir.setReturnValue(true);
    }

    @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
    public void filament$customBurnDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (FuelRegistry.getCache().containsKey(itemStack.getItem()))
            cir.setReturnValue(FuelRegistry.getCache().get(itemStack.getItem()));
    }
}
