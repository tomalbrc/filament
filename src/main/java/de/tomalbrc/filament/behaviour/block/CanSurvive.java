package de.tomalbrc.filament.behaviour.block;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CanSurvive implements BlockBehaviour<CanSurvive.Config> {
    private final Config config;

    public CanSurvive(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public CanSurvive.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction.Axis axis = getAxisOrNull(blockState);
        if (axis != null) {
            return test(axis, blockPos, levelReader, blockState);
        }

        Direction direction = getFacing(blockState);
        return test(direction, blockPos, levelReader, blockState);
    }

    private boolean test(Direction direction, BlockPos blockPos, LevelReader levelReader, BlockState blockState) {
        var belowState = levelReader.getBlockState(blockPos.relative(direction));
        if (this.config.blocks != null) {
            for (Identifier Identifier : this.config.blocks) {
                var block = BuiltInRegistries.BLOCK.getValue(Identifier);
                if (belowState.is(block)) {
                    return !belowState.is(Blocks.WATER) || levelReader.getFluidState(blockPos.relative(direction)).isSource();
                }
            }
        }

        if (this.config.tags != null) {
            for (Identifier tag : this.config.tags) {
                var tagKey = TagKey.create(Registries.BLOCK, tag);
                if (belowState.is(tagKey))
                    return true;
            }
        }

        return false;
    }

    private boolean test(Direction.Axis axis, BlockPos blockPos, LevelReader levelReader, BlockState blockState) {
        for (Direction direction : axis.getPlane()) {
            if (test(direction, blockPos, levelReader, blockState)) {
                return true;
            }
        }

        return false;
    }

    private Direction.Axis getAxisOrNull(BlockState blockState) {
        List<EnumProperty<Direction.Axis>> props2 = ImmutableList.of(BlockStateProperties.AXIS, BlockStateProperties.HORIZONTAL_AXIS);
        for (var prop: props2) {
            if (blockState.hasProperty(prop)) {
                return blockState.getValue(prop);
            }
        }

        return null;
    }

    private Direction getFacing(BlockState blockState) {
        List<EnumProperty<Direction>> props = ImmutableList.of(BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.FACING_HOPPER);
        for (var prop: props) {
            if (blockState.hasProperty(prop)) {
                return blockState.getValue(prop);
            }
        }
        return Direction.DOWN;
    }


    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return !blockState.canSurvive(levelReader, blockPos) ? Blocks.AIR.defaultBlockState() : blockState;
    }

    public static class Config {
        public List<Identifier> blocks;
        public List<Identifier> tags;
    }
}
