package de.tomalbrc.filament.behaviour.block;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.mixin.accessor.DoublePlantBlockInvoker;
import de.tomalbrc.filament.util.FilamentBlockResourceUtils;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Door implements BlockBehaviour<Door.Config> {
    private final Config config;

    public Door(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        DoubleBlockHalf doubleBlockHalf = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (isSame(blockState2.getBlock()) && blockState2.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != doubleBlockHalf) {
                return blockState2.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, doubleBlockHalf);
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return BlockBehaviour.super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (explosion.canTriggerBlocks() && blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER && this.config.canOpenByWindCharge && !blockState.getValue(BlockStateProperties.POWERED)) {
            this.setOpen(null, level, blockState, blockPos, !this.isOpen(blockState));
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!(level.isClientSide() || !player.isCreative() && player.hasCorrectToolForDrops(blockState))) {
            DoublePlantBlockInvoker.invokePreventDropFromBottomPart(level, blockPos, blockState, player);
        }
    }

    @Override
    public Optional<Boolean> isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case PathComputationType.LAND, PathComputationType.AIR -> Optional.of(blockState.getValue(BlockStateProperties.OPEN));
            case PathComputationType.WATER -> Optional.of(false);
        };
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockState self, BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        if (level.isInsideBuildHeight(blockPos.getY()) && level.getBlockState(blockPos.above()).canBeReplaced(blockPlaceContext)) {
            boolean powered = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
            return self.getBlock().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection())
                    .setValue(BlockStateProperties.DOOR_HINGE, this.getHinge(blockPlaceContext))
                    .setValue(BlockStateProperties.POWERED, powered).setValue(BlockStateProperties.OPEN, powered)
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        level.setBlock(blockPos.above(), blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    private boolean isSame(Block block) {
        return (block.isFilamentBlock() && block.has(Behaviours.DOOR)) || block instanceof DoorBlock;
    }

    private DoorHingeSide getHinge(BlockPlaceContext blockPlaceContext) {
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Direction direction = blockPlaceContext.getHorizontalDirection();
        BlockPos blockPos2 = blockPos.above();
        Direction direction2 = direction.getCounterClockWise();
        BlockPos blockPos3 = blockPos.relative(direction2);
        BlockState blockState = blockGetter.getBlockState(blockPos3);
        BlockPos blockPos4 = blockPos2.relative(direction2);
        BlockState blockState2 = blockGetter.getBlockState(blockPos4);
        Direction direction3 = direction.getClockWise();
        BlockPos blockPos5 = blockPos.relative(direction3);
        BlockState blockState3 = blockGetter.getBlockState(blockPos5);
        BlockPos blockPos6 = blockPos2.relative(direction3);
        BlockState blockState4 = blockGetter.getBlockState(blockPos6);
        int i = (blockState.isCollisionShapeFullBlock(blockGetter, blockPos3) ? -1 : 0) + (blockState2.isCollisionShapeFullBlock(blockGetter, blockPos4) ? -1 : 0) + (blockState3.isCollisionShapeFullBlock(blockGetter, blockPos5) ? 1 : 0) + (blockState4.isCollisionShapeFullBlock(blockGetter, blockPos6) ? 1 : 0);
        boolean bl = isSame(blockState.getBlock()) && blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        boolean bl2 = isSame(blockState3.getBlock()) && blockState3.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        if (bl && !bl2 || i > 0) {
            return DoorHingeSide.RIGHT;
        }
        if (bl2 && !bl || i < 0) {
            return DoorHingeSide.LEFT;
        }
        int j = direction.getStepX();
        int k = direction.getStepZ();
        Vec3 vec3 = blockPlaceContext.getClickLocation();
        double d = vec3.x - (double)blockPos.getX();
        double e = vec3.z - (double)blockPos.getZ();
        return j < 0 && e < 0.5 || j > 0 && e > 0.5 || k < 0 && d > 0.5 || k > 0 && d < 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!this.config.canOpenByHand) {
            return InteractionResult.PASS;
        }
        blockState = blockState.cycle(BlockStateProperties.OPEN);
        level.setBlock(blockPos, blockState, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        this.playSound(player, level, blockPos, blockState.getValue(BlockStateProperties.OPEN));
        level.gameEvent(player, this.isOpen(blockState) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        return InteractionResult.SUCCESS_SERVER;
    }

    public boolean isOpen(BlockState blockState) {
        return blockState.getValue(BlockStateProperties.OPEN);
    }

    public void setOpen(@Nullable Entity entity, Level level, BlockState blockState, BlockPos blockPos, boolean bl) {
        if (!(blockState.getBlock().isFilamentBlock() && blockState.getBlock().has(Behaviours.DOOR)) || blockState.getValue(BlockStateProperties.OPEN) == bl) {
            return;
        }
        level.setBlock(blockPos, blockState.setValue(BlockStateProperties.OPEN, bl), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        this.playSound(entity, level, blockPos, bl);
        level.gameEvent(entity, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, Orientation orientation, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.relative(blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (!blockState.getBlock().defaultBlockState().is(block) && bl2 != blockState.getValue(BlockStateProperties.POWERED)) {
            if (bl2 != blockState.getValue(BlockStateProperties.OPEN)) {
                this.playSound(null, level, blockPos, bl2);
                level.gameEvent(null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
            }

            level.setBlock(blockPos, blockState.setValue(BlockStateProperties.POWERED, bl2).setValue(BlockStateProperties.OPEN, bl2), Block.UPDATE_CLIENTS);
        }

    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        if (blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
            return blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP);
        }
        return blockState2.getBlock().isFilamentBlock() && blockState2.getBlock().has(Behaviours.DOOR);
    }

    private void playSound(@Nullable Entity entity, Level level, BlockPos blockPos, boolean open) {
        level.playSound(entity, blockPos, SoundEvent.createVariableRangeEvent(open ? this.config.openSound : this.config.closeSound), SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return blockState;
        }
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING))).cycle(BlockStateProperties.DOOR_HINGE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Optional<Long> getSeed(BlockState blockState, BlockPos blockPos) {
        return Optional.of(Mth.getSeed(blockPos.getX(), blockPos.below(blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), blockPos.getZ()));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN, BlockStateProperties.DOOR_HINGE, BlockStateProperties.POWERED);
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).setValue(BlockStateProperties.OPEN, false).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> data) {
        if (data.blockResource() != null) for (Map.Entry<String, PolymerBlockModel> entry : data.blockResource().models().entrySet()) {
            PolymerBlockModel blockModel = entry.getValue();

            BlockStateParser.BlockResult parsed;
            String str = String.format("%s[%s]", data.id(), entry.getKey());
            try {
                parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid BlockState value: " + str);
            }

            map.put(parsed.blockState(), BlockData.BlockStateMeta.of(doorState(parsed, blockModel, data.virtual()), blockModel));
        }

        return true;
    }

    private BlockState doorState(BlockStateParser.BlockResult parsed, PolymerBlockModel blockModel, boolean virtual) {
        BlockState requestedState = null;

        if (parsed.blockState().getValue(BlockStateProperties.OPEN) && parsed.blockState().getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.RIGHT) {
            if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.WEST_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.NORTH_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.EAST_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.SOUTH_DOOR, blockModel, virtual);
            }
        } else if (parsed.blockState().getValue(BlockStateProperties.OPEN) && parsed.blockState().getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT) {
            if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.EAST_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.SOUTH_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.WEST_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.NORTH_DOOR, blockModel, virtual);
            }
        } else {
            if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.NORTH_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.EAST_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.SOUTH_DOOR, blockModel, virtual);
            } else if (parsed.blockState().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST) {
                requestedState = FilamentBlockResourceUtils.requestBlock(BlockModelType.WEST_DOOR, blockModel, virtual);
            }
        }

        return requestedState;
    }

    @Override
    public BlockState modifyPolymerBlockState(BlockState original, BlockState blockState) {
        return blockState.setValue(BlockStateProperties.POWERED, false);
    }

    public static class Config {
        public boolean canOpenByWindCharge = true;
        public boolean canOpenByHand = true;
        public ResourceLocation openSound = SoundEvents.WOODEN_DOOR_OPEN.location();
        public ResourceLocation closeSound = SoundEvents.WOODEN_DOOR_CLOSE.location();
    }
}