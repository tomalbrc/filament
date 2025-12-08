package de.tomalbrc.filament.mixin.behaviour.fire;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Fire;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Invoker
    protected static int invokeGetFireTickDelay(RandomSource randomSource) {
        throw new AssertionError();
    }

    @Inject(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"), cancellable = true)
    private void filament$onFirePlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl, CallbackInfo ci) {
        Fire f;
        if (blockState.getBlock().isFilamentBlock() && (f = blockState.getBlock().get(Behaviours.FIRE)) != null && f.getConfig().tick) {
            level.scheduleTick(blockPos, blockState.getBlock(), invokeGetFireTickDelay(level.random));
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private void filament$onTick(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        Fire f;
        if (blockState.getBlock().isFilamentBlock() && (f = blockState.getBlock().get(Behaviours.FIRE)) != null && f.getConfig().tick) {
            level.scheduleTick(blockPos, blockState.getBlock(), invokeGetFireTickDelay(level.random));
        }
    }
}
