package de.tomalbrc.filament.behaviour.block;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Budding implements BlockBehaviour<Budding.Config>, SimpleWaterloggedBlock {
    private final Config config;

    public Budding(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Budding.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(101) < this.config.chance) {
            return;
        }

        Direction direction = this.config.sides.get(randomSource.nextInt(this.config.sides.size()));
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = serverLevel.getBlockState(blockPos2);
        BlockState state = null;

        if (this.canGrowAndReplace(blockState2)) {
            state = BuiltInRegistries.BLOCK.get(this.config.grows.getFirst()).defaultBlockState();
        }

        Direction dir = getFacingOrNull(blockState2);
        for (int i = 0; i < this.config.grows.size()-1; i++) {
            var currentStage = BuiltInRegistries.BLOCK.get(this.config.grows.get(i));
            if (blockState2.is(currentStage) && (dir == null || dir == direction)) {
                var nextStage = BuiltInRegistries.BLOCK.get(this.config.grows.get(i + 1));
                state = nextStage.defaultBlockState();
                break;
            }
        }

        if (state != null) {
            BlockState finalState = setWaterlogged(setFacing(state, direction), blockState2.getFluidState().getType() == Fluids.WATER);
            serverLevel.setBlockAndUpdate(blockPos2, finalState);
        }
    }

    private Direction getFacingOrNull(BlockState blockState) {
        List<DirectionProperty> props = ImmutableList.of(BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.FACING_HOPPER);
        for (var prop: props) {
            if (blockState.hasProperty(prop)) {
                return blockState.getValue(prop);
            }
        }

        return null;
    }

    private BlockState setFacing(BlockState blockState, Direction direction) {
        List<DirectionProperty> props = ImmutableList.of(BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.FACING_HOPPER);
        for (var prop: props) {
            if (blockState.hasProperty(prop)) {
                return blockState.setValue(prop, direction);
            }
        }

        return blockState;
    }

    private BlockState setWaterlogged(BlockState blockState, boolean waterlogged) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return blockState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }

        return blockState;
    }

    private boolean canGrowAndReplace(BlockState blockState) {
        return blockState.isAir() || BuiltInRegistries.BLOCK.get(this.config.grows.getFirst()).defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED) && blockState.is(Blocks.WATER) && blockState.getFluidState().getAmount() == 8;
    }

    public static class Config {
        public int chance = 20;
        public List<ResourceLocation> grows = ImmutableList.of(ResourceLocation.withDefaultNamespace("chain"), ResourceLocation.withDefaultNamespace("end_rod"));
        public List<Direction> sides = Arrays.stream(Direction.values()).toList();
    }
}