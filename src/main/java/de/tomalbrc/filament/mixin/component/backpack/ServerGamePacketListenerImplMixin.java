package de.tomalbrc.filament.mixin.component.backpack;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.registry.FilamentComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void filament$backpackInteraction(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.getCount() == 1 && itemStack.has(FilamentComponents.BACKPACK) && itemStack.has(DataComponents.CONTAINER)) {
            var backpackOptions = itemStack.get(FilamentComponents.BACKPACK);
            assert backpackOptions != null;
            backpackOptions.open(itemStack, player);
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void filament$backpackInteraction(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.getCount() == 1 && itemStack.has(FilamentComponents.BACKPACK)) {
            var backpackOptions = itemStack.get(FilamentComponents.BACKPACK);
            assert backpackOptions != null;
            if (backpackOptions.preventPlacement()) {
                backpackOptions.open(itemStack, player);
                ci.cancel();
            }
        }
    }
}
