package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class LeafDecay implements BlockBehaviour<LeafDecay.Config> {
    private final Config config;

    public LeafDecay(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public LeafDecay.Config getConfig() {
        return this.config;
    }

    @Override
    public int getLightBlock(BlockState state) {
        return 1;
    }

    @Override
    public BlockState filteredBlockState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.DISTANCE, 7).setValue(BlockStateProperties.PERSISTENT, false);
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.DISTANCE, 7).setValue(BlockStateProperties.PERSISTENT, false);
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.DISTANCE, BlockStateProperties.PERSISTENT);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState selfDefault, BlockPlaceContext context) {
        BlockState blockState = (selfDefault.setValue(BlockStateProperties.PERSISTENT, true));
        return updateDistance(blockState, context.getLevel(), context.getClickedPos());
    }


    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(BlockStateProperties.DISTANCE) == 7 && !(Boolean)state.getValue(BlockStateProperties.PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.decaying(state)) {
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    protected boolean decaying(BlockState state) {
        return !state.getValue(BlockStateProperties.PERSISTENT) && state.getValue(BlockStateProperties.DISTANCE) == 7;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        int dist = getDistanceAt(blockState2) + 1;
        if (dist != 1 || blockState.getValue(BlockStateProperties.DISTANCE) != dist) {
            scheduledTickAccess.scheduleTick(blockPos, blockState.getBlock(), 1);
        }
        return blockState;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, updateDistance(state, level, pos), Block.UPDATE_ALL);
    }

    private static BlockState updateDistance(BlockState state, LevelAccessor level, BlockPos pos) {
        int dist = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(pos, direction);
            dist = Math.min(dist, getDistanceAt(level.getBlockState(mutableBlockPos)) + 1);
            if (dist == 1) {
                break;
            }
        }

        return state.setValue(BlockStateProperties.DISTANCE, dist);
    }

    private static int getDistanceAt(BlockState neighbor) {
        return LeavesBlock.getOptionalDistanceAt(neighbor).orElse(7);
    }

    public static class Config {

    }
}