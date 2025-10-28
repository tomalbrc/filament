package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void filament$val(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (item instanceof FilamentItem filamentItem) {
            filamentItem.verifyComponentsAfterLoad((ItemStack)((Object) this));
        }
    }
}
