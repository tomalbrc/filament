package de.tomalbrc.filament.behaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface AsyncTickingBlockBehaviour {
    /**
     * Async tick
     * @param blockState state of ticking block
     * @param serverLevel level
     * @param blockPos position
     * @param randomSource random source
     */
    void tickAsync(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource);
}
