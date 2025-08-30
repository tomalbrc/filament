package de.tomalbrc.filament.mixin;

import com.mojang.serialization.DataResult;
import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "validateStrict", at = @At("HEAD"))
    private static void filament$val(ItemStack itemStack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        if (itemStack.getItem() instanceof FilamentItem filamentItem) {
            filamentItem.verifyComponentsAfterLoad(itemStack);
        }
    }
}
