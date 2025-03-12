package de.tomalbrc.filament.mixin.behaviour.grass_spread;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpreadingSnowyDirtBlock.class)
public interface SpreadingSnowyDirtBlockAccessor {
    @Invoker
    static boolean invokeCanBeGrass(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static boolean invokeCanPropagate(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new UnsupportedOperationException();
    }
}
