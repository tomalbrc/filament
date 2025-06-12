package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class Connectable implements DecorationBehaviour<Connectable.Config> {
    static StringRepresentable.StringRepresentableCodec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

    private final Config config;

    private Block block;

    private Shape shape = Shape.SINGLE;

    public Connectable(Config config) {
        this.config = config;
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        this.block = blockEntity.getBlockState().getBlock();
    }

    @Override
    public @NotNull Connectable.Config getConfig() {
        return this.config;
    }

    @Override
    public void read(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        compoundTag.read("shape", CODEC, provider.createSerializationContext(NbtOps.INSTANCE)).ifPresent(value -> this.shape = value);
    }

    @Override
    public void write(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        compoundTag.store("shape", CODEC, provider.createSerializationContext(NbtOps.INSTANCE), this.shape);
    }

    @Override
    public ItemStack visualItemStack(DecorationBlockEntity decorationBlockEntity, ItemStack itemStack) {
        //Filament.LOGGER.info("Custom shape: {}", shape.toString());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(shape.customModelData()), List.of()));
        return itemStack;
    }

    @Override
    public BlockState updateShape(DecorationBlockEntity blockEntity, BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction dir, BlockPos neighbourPos, BlockState neighbourState, RandomSource randomSource) {
        if (blockEntity == null || !dir.getAxis().isHorizontal())
            return blockState;

        Direction direction = Direction.fromYRot((blockEntity).getVisualRotationYInDegrees());
        if (direction.getAxis().isHorizontal()) {
            var newShape = getStairsShape(blockEntity, levelReader, blockPos);
            if (newShape != this.shape) {
                this.shape = newShape;
                blockEntity.updateModel();
                blockEntity.setChanged();
            }
        }

        return blockState;
    }

    public static class Config {
        public boolean corners = true;
    }

    private Shape getStairsShape(DecorationBlockEntity state, LevelReader level, BlockPos pos) {
        Direction facing = state.getFacing();
        Direction leftSide = facing.getCounterClockWise();
        Direction rightSide = facing.getClockWise();

        // outer corners (front connections)
        BlockEntity frontState = level.getBlockEntity(pos.relative(facing));
        if (isSameType(frontState)) {
            Direction frontFacing = ((DecorationBlockEntity)frontState).getFacing();
            boolean isFrontLeftCorner = frontFacing == facing.getCounterClockWise();
            boolean isFrontRightCorner = frontFacing == facing.getClockWise();

            if (isFrontLeftCorner && canTakeShape(state, level, pos, frontFacing.getOpposite())) {
                // outer left corner, need connection on right side
                BlockEntity rightState = level.getBlockEntity(pos.relative(rightSide));
                if (isSameType(rightState) && getFacing(rightState) == facing) {
                    return Shape.OUTER_LEFT;
                }
            }
            else if (isFrontRightCorner && canTakeShape(state, level, pos, frontFacing.getOpposite())) {
                // outer right corner, need connection on left side
                BlockEntity leftState = level.getBlockEntity(pos.relative(leftSide));
                if (isSameType(leftState) && getFacing(leftState) == facing) {
                    return Shape.OUTER_RIGHT;
                }
            }
        }

        // for inner corners (back connections)
        BlockEntity backState = level.getBlockEntity(pos.relative(facing.getOpposite()));
        if (isSameType(backState)) {
            Direction backFacing = getFacing(backState);
            boolean isBackLeftCorner = backFacing == facing.getCounterClockWise();
            boolean isBackRightCorner = backFacing == facing.getClockWise();

            if (isBackLeftCorner && canTakeShape(state, level, pos, backFacing)) {
                // inner left corner, need connection on right side
                BlockEntity rightState = level.getBlockEntity(pos.relative(rightSide));
                if (isSameType(rightState) && getFacing(rightState) == facing) {
                    return Shape.INNER_LEFT;
                }
            }
            else if (isBackRightCorner && canTakeShape(state, level, pos, backFacing)) {
                // inner right corner, need connection on left side
                BlockEntity leftState = level.getBlockEntity(pos.relative(leftSide));
                if (isSameType(leftState) && getFacing(leftState) == facing) {
                    return Shape.INNER_RIGHT;
                }
            }
        }

        // row connections (left/right/straight/single)
        BlockEntity leftState = level.getBlockEntity(pos.relative(leftSide));
        BlockEntity rightState = level.getBlockEntity(pos.relative(rightSide));

        boolean leftConnected = isSameType(leftState) && getFacing(leftState) == facing;
        boolean rightConnected = isSameType(rightState) && getFacing(rightState) == facing;

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

    private boolean canTakeShape(BlockEntity state, LevelReader level, BlockPos pos, Direction face) {
        BlockEntity adjacent = level.getBlockEntity(pos.relative(face));
        return isSameType(adjacent) && getFacing(adjacent) != getFacing(state);
    }

    private boolean isSameType(@Nullable BlockEntity state) {
        return state != null && state.getBlockState().getBlock() == this.block;
    }

    static Direction getFacing(BlockEntity blockEntity) {
        return ((DecorationBlockEntity)blockEntity).getFacing();
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
