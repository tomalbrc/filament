package de.tomalbrc.filament.mixin.decoration;

import de.tomalbrc.filament.decoration.util.VirtualCollisionTracker;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void filament$acceptVirtualCollision(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (this.player == null) return;
        if (this.player.onGround()) return;

        if (!VirtualCollisionTracker.hasSupportBelow(this.player)) return;

        this.player.setOnGround(true);

        Vec3 velocity = this.player.getDeltaMovement();
        if (velocity.y < 0) {
            this.player.setDeltaMovement(velocity.x, 0, velocity.z);
        }
    }
}