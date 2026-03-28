package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void filament$val(Holder<Item> item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (item instanceof FilamentItem filamentItem) {
            filamentItem.verifyComponentsAfterLoad((ItemStack)((Object) this));
        }
    }
}
