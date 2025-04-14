package de.tomalbrc.filament.mixin.behaviour.execute;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.Execute;
import de.tomalbrc.filament.item.SimpleItem;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
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

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void filament$handleSwing(ServerboundSwingPacket serverboundSwingPacket, CallbackInfo ci) {
        ItemStack item = this.player.getItemInHand(serverboundSwingPacket.getHand());
        if (item.getItem() instanceof SimpleItem simpleItem) {
            Execute ex = simpleItem.get(Behaviours.EXECUTE_ATTACK);
            if (ex != null) {
                ex.runCommandItem(this.player, simpleItem, serverboundSwingPacket.getHand());
            }
        }
    }
}
