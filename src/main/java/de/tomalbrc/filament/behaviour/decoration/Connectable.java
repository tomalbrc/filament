package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Connectable implements BlockBehaviour<Connectable.Config>, DecorationRotationProvider, DecorationBehaviour<Connectable.Config> {
    private final Config config;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    private Block block;

    public Connectable(Config config) {
        this.config = config;
    }

    @Override
    public @NotNull Connectable.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        this.block = block;
    }

    @Override
    public ItemStack visualItemStack(DecorationBlockEntity decorationBlockEntity, ItemStack itemStack, BlockState blockState) {
        Filament.LOGGER.info("Item stack update at {}: {}", decorationBlockEntity.getBlockPos(), blockState.getValue(SHAPE).customModelData());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(blockState.getValue(SHAPE).customModelData()), List.of()));
        return itemStack;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction dir, BlockPos neighbourPos, BlockState neighbourState, RandomSource randomSource) {
        Direction direction = blockState.getValue(FACING);
        if (direction.getAxis().isHorizontal()) {
            var newShape = getShape(blockState, levelReader, blockPos);
            Filament.LOGGER.info("new updateShape shape at {}: {}", blockPos, newShape.customModelData());

            return blockState.setValue(SHAPE, newShape);
        }

        return blockState;
    }

    @Override
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return BlockBehaviour.super.getCloneItemStack(itemStack, levelReader, blockPos, blockState);
    }

    public static class Config {
        public boolean corners = true;
    }

    private Shape getShape(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction leftSide = facing.getCounterClockWise();
        Direction rightSide = facing.getClockWise();

        if (config.corners) {
            // outer corners (front connections)
            BlockState frontState = level.getBlockState(pos.relative(facing.getOpposite()));
            if (isSameType(frontState)) {
                Direction frontFacing = frontState.getValue(FACING);
                boolean isFrontLeftCorner = frontFacing == facing.getCounterClockWise();
                boolean isFrontRightCorner = frontFacing == facing.getClockWise();

                if (isFrontLeftCorner && canTakeCornerShape(state, level, pos, frontFacing.getOpposite())) {
                    return Shape.OUTER_LEFT;
                }
                else if (isFrontRightCorner && canTakeCornerShape(state, level, pos, frontFacing.getOpposite())) {
                    return Shape.OUTER_RIGHT;
                }
            }

            // for inner corners (back connections)
            BlockState backState = level.getBlockState(pos.relative(facing));
            if (isSameType(backState)) {
                Direction backFacing = backState.getValue(FACING);
                boolean isBackLeftCorner = backFacing == facing.getCounterClockWise();
                boolean isBackRightCorner = backFacing == facing.getClockWise();

                if (isBackLeftCorner && canTakeCornerShape(state, level, pos, backFacing)) {
                    return Shape.INNER_LEFT;
                }
                else if (isBackRightCorner && canTakeCornerShape(state, level, pos, backFacing)) {
                    return Shape.INNER_RIGHT;
                }
            }
        }

        // row connections (left/right/straight/single)
        BlockState leftState = level.getBlockState(pos.relative(leftSide));
        BlockState rightState = level.getBlockState(pos.relative(rightSide));

        boolean leftConnected = isSameType(leftState) && (leftState.getValue(FACING) == facing || leftState.getValue(FACING) == facing.getClockWise() || leftState.getValue(FACING) == facing.getCounterClockWise());
        boolean rightConnected = isSameType(rightState) && (rightState.getValue(FACING) == facing || rightState.getValue(FACING) == facing.getClockWise() || rightState.getValue(FACING) == facing.getCounterClockWise());

        if (leftConnected && rightConnected) {
            return Shape.STRAIGHT;
        } else if (leftConnected) {
            return Shape.RIGHT;
        } else if (rightConnected) {
            return Shape.LEFT;
        } else {
            return Shape.SINGLE;
        }
    }

    private boolean canTakeCornerShape(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        BlockState adjacent = level.getBlockState(pos.relative(direction));
        return isSameType(adjacent) && (adjacent.getValue(FACING) == state.getValue(FACING) || adjacent.getValue(FACING) == state.getValue(FACING).getCounterClockWise() || adjacent.getValue(FACING) == state.getValue(FACING).getClockWise());
    }

    private boolean isSameType(@Nullable BlockState state) {
        return state != null && state.getBlock() == this.block;
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
        blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
        return blockState.setValue(SHAPE, getShape(blockState, context.getLevel(), context.getClickedPos()));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public float getVisualRotationYInDegrees(BlockState blockState) {
        var facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var shape = blockState.getValue(SHAPE);

        if ((shape == Shape.INNER_RIGHT || shape == Shape.OUTER_RIGHT) && (facing.getAxis() == Direction.Axis.X || facing.getAxis() == Direction.Axis.Z)) {
            return facing.getOpposite().toYRot()+90;
        }

        return facing.getOpposite().toYRot();
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }


    public enum Shape implements StringRepresentable {
        STRAIGHT("middle", "middle", 0),
        INNER_LEFT("inner_left", "inner", 2),
        INNER_RIGHT("inner_right", "inner", 0),
        OUTER_LEFT("outer_left", "outer", 2),
        OUTER_RIGHT("outer_right", "outer", 0),
        LEFT("left", "left", 0),
        RIGHT("right", "right", 0),
        SINGLE("single", "single", 0);

        private final String name;
        private final String customModelData;
        private final int rotation;

        Shape(final String name, String customModelData, int rotation) {
            this.name = name;
            this.customModelData = customModelData;
            this.rotation = rotation;
        }

        public String customModelData() {
            return this.customModelData;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public int rotation() {
            return this.rotation;
        }
    }
}
