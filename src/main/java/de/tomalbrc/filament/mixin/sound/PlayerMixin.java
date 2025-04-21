package de.tomalbrc.filament.mixin.sound;

import com.mojang.authlib.GameProfile;
import de.tomalbrc.filament.mixin.accessor.EntityInvoker;
import de.tomalbrc.filament.util.FilamentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// enable step sounds
@Mixin(ServerPlayer.class)
public abstract class PlayerMixin extends Player {
    public PlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "checkMovementStatistics", at = @At("HEAD"))
    public void filament$checkMovementStatistics(double d, double e, double f, CallbackInfo ci) {
        // run step sound checks etc
        if (FilamentConfig.getInstance().soundModule && !isPassenger() && d != 0 && e != 0 && f != 0) ((EntityInvoker)this).invokeApplyMovementEmissionAndPlaySound(MovementEmission.SOUNDS, new Vec3(d,e,f), this.getOnPos(), this.getBlockStateOn());
    }
}
