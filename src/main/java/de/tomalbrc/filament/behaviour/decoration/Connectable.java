package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.item.FilamentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (decorationBlockEntity.getItem().getItem() instanceof FilamentItem filamentItem) {
            filamentItem.getModelData();
            var c = blockState.getValue(SHAPE).customModelData();
            if (filamentItem.getModelData().containsKey(c)) {
                itemStack.set(DataComponents.CUSTOM_MODEL_DATA, filamentItem.getModelData().get(c).asComponent());
            }
        }
        return itemStack;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction dir, BlockState neighbourState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighbourPos) {
        Direction direction = blockState.getValue(FACING);
        if (direction.getAxis().isHorizontal()) {
            var newShape = getShape(blockState, levelAccessor, blockPos);
            return blockState.setValue(SHAPE, newShape);
        }

        return blockState;
    }

    @Override
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return DecorationBehaviour.super.getCloneItemStack(itemStack, levelReader, blockPos, blockState, includeData);
    }

    private Shape getShape(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction leftSide = facing.getCounterClockWise();
        Direction rightSide = facing.getClockWise();

        // row connections (left/right/straight/single)
        BlockState leftState = level.getBlockState(pos.relative(leftSide));
        BlockState rightState = level.getBlockState(pos.relative(rightSide));

        boolean leftConnected = isSameType(leftState) && (leftState.getValue(FACING) == facing || leftState.getValue(FACING) == facing.getClockWise() && leftState.getValue(SHAPE).cornerOuter || leftState.getValue(FACING) == facing.getCounterClockWise() && leftState.getValue(SHAPE).cornerOuter);
        boolean rightConnected = isSameType(rightState) && (rightState.getValue(FACING) == facing || rightState.getValue(FACING) == facing.getClockWise() && rightState.getValue(SHAPE).cornerInner || rightState.getValue(FACING) == facing.getCounterClockWise() && rightState.getValue(SHAPE).cornerInner);

        if (leftConnected && rightConnected) {
            return Shape.STRAIGHT;
        }

        if (config.corners) {
            // outer corners (front connections)
            BlockState frontState = level.getBlockState(pos.relative(facing.getOpposite()));
            if (isSameType(frontState) && !frontState.getValue(SHAPE).isCorner()) {
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
            if (isSameType(backState) && !backState.getValue(SHAPE).isCorner()) {
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

        if (leftConnected) {
            return Shape.RIGHT;
        } else if (rightConnected) {
            return Shape.LEFT;
        }

        return Shape.SINGLE;
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
        STRAIGHT("middle", "middle", false, false),
        INNER_LEFT("inner_left", "inner", false, true),
        INNER_RIGHT("inner_right", "inner", true, false),
        OUTER_LEFT("outer_left", "outer", false, true),
        OUTER_RIGHT("outer_right", "outer", true, false),
        LEFT("left", "left", false, false),
        RIGHT("right", "right", false, false),
        SINGLE("single", "single", false, false);

        private final String name;
        private final String customModelData;
        private final boolean cornerOuter;
        private final boolean cornerInner;

        Shape(final String name, String customModelData, boolean cornerOuter, boolean cornerInner) {
            this.name = name;
            this.customModelData = customModelData;
            this.cornerOuter = cornerOuter;
            this.cornerInner = cornerInner;
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

        public boolean isCorner() {
            return cornerInner || cornerOuter;
        }
    }

    public static class Config {
        public boolean corners = true;
    }
}
