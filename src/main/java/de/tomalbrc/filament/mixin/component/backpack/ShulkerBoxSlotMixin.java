package de.tomalbrc.filament.mixin.component.backpack;

import de.tomalbrc.filament.registry.FilamentComponents;
import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public abstract class ShulkerBoxSlotMixin extends Slot {
    public ShulkerBoxSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    protected void filament$containerFix(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.has(FilamentComponents.BACKPACK) || FilamentContainer.isPickUpContainer(container)) cir.setReturnValue(false);
    }
}
