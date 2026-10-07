package de.tomalbrc.filament.mixin.decoration;

import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.DecorationPreviewManager;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            ), cancellable = true
    )
    private void filament$afterOffhandSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) return;
        if (!player.isShiftKeyDown()) return;

        if (!(player.getMainHandItem().getItem() instanceof DecorationItem)) return;

        DecorationPreviewManager.toggle(player);
        ci.cancel();
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void filament$clearPreview(DisconnectionDetails details, CallbackInfo ci) {
        DecorationPreviewManager.clear(this.player);
    }
}