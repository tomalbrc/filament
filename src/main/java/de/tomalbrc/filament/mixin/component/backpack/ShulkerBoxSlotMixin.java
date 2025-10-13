package de.tomalbrc.filament.mixin.component.backpack;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    protected void filament$containerFix(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.has(FilamentComponents.BACKPACK)) cir.setReturnValue(false);
    }
}
