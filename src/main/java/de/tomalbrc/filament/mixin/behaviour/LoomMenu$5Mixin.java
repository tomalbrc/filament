package de.tomalbrc.filament.mixin.behaviour;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.LoomMenu$5")
public class LoomMenu$5Mixin {
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void filament$allowCustomItems(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.getItem() instanceof SimpleItem simpleItem && simpleItem.has(Behaviours.BANNER_PATTERN))
            cir.setReturnValue(true);
    }
}