package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Waterloggable implements BlockBehaviour<Waterloggable.Config>, SimpleWaterloggedBlock {
    private final Config config;

    public Waterloggable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Waterloggable.Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
        return blockState.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return null;
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity livingEntity, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return SimpleWaterloggedBlock.super.canPlaceLiquid(livingEntity, blockGetter, blockPos, blockState, fluid);
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return blockState;
    }

    @Override
    public Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> Optional.of(false);
            case WATER -> Optional.of(blockState.getFluidState().is(FluidTags.WATER));
        };
    }

    @Override
    public BlockState modifyPolymerBlockState(BlockState originalState, BlockState blockState) {
        boolean isAir = blockState.is(Blocks.AIR);
        boolean waterlogged = originalState.hasProperty(BlockStateProperties.WATERLOGGED) && originalState.getValue(BlockStateProperties.WATERLOGGED);

        if (isAir && waterlogged) {
            return Blocks.WATER.defaultBlockState();
        } else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }

        return blockState;
    }

    public static class Config {}
}