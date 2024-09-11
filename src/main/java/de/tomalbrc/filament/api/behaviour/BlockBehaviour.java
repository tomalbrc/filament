package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface BlockBehaviour<T> extends Behaviour<T> {
    default void init(Block block, BehaviourHolder behaviourHolder) {

    }

    default boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, BlockData blockData) {
        return false;
    }

    default BlockState modifyDefaultState(BlockState blockState) {
        return blockState;
    }

    default boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        return false;
    }

    default Optional<Boolean> useShapeForLightOcclusion(BlockState blockState) {
        return Optional.empty();
    }

    default BlockState getStateForPlacement(BlockState block, BlockPlaceContext blockPlaceContext) {
        return block;
    }

    default Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return Optional.empty();
    }

    default BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState;
    }

    default void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
    }

    default Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return Optional.empty();
    }

    @Nullable
    default FluidState getFluidState(BlockState blockState) {
        return null;
    }

    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return null;
    }

    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return null;
    }

    default boolean isSignalSource(BlockState blockState) {
        return false;
    }

    default int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    default int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    default void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    default void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    default boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    default boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    default void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    default void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    default BlockState getCustomPolymerBlockState(Map<BlockState, BlockData.BlockStateMeta> stateMap, BlockState blockState) {
        return null;
    }

    default ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return null;
    }
}