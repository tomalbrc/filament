package de.tomalbrc.filament.mixin.component.skin;

import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isBroken()Z"))
    private void filament$dontDestroySkin(int i, ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci) {
        var self = ItemStack.class.cast(this);
        if (self.isBroken() && self.has(FilamentComponents.SKIN_DATA_COMPONENT)) {
            var skinItem = self.get(FilamentComponents.SKIN_DATA_COMPONENT);
            if (skinItem != null && !skinItem.isEmpty()) {
                if (!serverPlayer.addItem(skinItem))
                    serverPlayer.spawnAtLocation(serverPlayer.serverLevel(), skinItem);

                self.remove(FilamentComponents.SKIN_DATA_COMPONENT);
            }
        }
    }
}
