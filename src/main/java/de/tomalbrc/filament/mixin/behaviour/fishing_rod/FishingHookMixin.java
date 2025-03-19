package de.tomalbrc.filament.mixin.behaviour.fishing_rod;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Inject(method = "shouldStopFishing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;discard()V"), cancellable = true)
    private void filament$customShouldStopFishing(Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack = player.getMainHandItem();
        ItemStack itemStack2 = player.getOffhandItem();
        if (isRod(itemStack) || isRod(itemStack2)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean isRod(ItemStack itemStack) {
        return itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.FISHING_ROD);
    }
}
