package de.tomalbrc.filament.mixin.behaviour.execute;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.item.ExecuteAttackItem;
import de.tomalbrc.filament.item.FilamentItem;
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
        if (item.getItem() instanceof FilamentItem simpleItem) {
            ExecuteAttackItem ex = simpleItem.get(Behaviours.ITEM_ATTACK_EXECUTE);
            if (ex != null && !ex.getConfig().onEntityAttack) {
                ex.runCommandItem(this.player, item.getItem(), serverboundSwingPacket.getHand());
            }
        }
    }
}
