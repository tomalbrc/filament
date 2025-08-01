package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker
    BlockState invokeUpdateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource);

    @Invoker
    BlockState invokeGetStateForPlacement(BlockGetter blockGetter, BlockPos blockPos);

    @Invoker
    boolean invokeCanSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos);

    @Invoker
    void invokeTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource);

    @Invoker
    void invokeOnPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl);

    @Invoker
    void invokeCreateBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);
}
