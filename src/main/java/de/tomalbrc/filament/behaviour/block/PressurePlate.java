package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PressurePlate implements BlockBehaviour<PressurePlate.Config> {
    private final Config config;

    public PressurePlate(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public PressurePlate.Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(PressurePlateBlock.POWERED, false);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignalForState(state);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (direction == Direction.UP) {
            return this.getSignalForState(state);
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    protected int getSignalForState(BlockState state) {
        return state.getValue(PressurePlateBlock.POWERED) ? 15 : 0;
    }

    protected BlockState setSignalForState(BlockState state, int signal) {
        return state.setValue(PressurePlateBlock.POWERED, signal > 0);
    }

    protected static final AABB AABB = Block.column(14.0, 0.0, 4.0).toAabbs().getFirst();

    protected int getSignalStrength(Level level, BlockPos pos) {
        Class<? extends Entity> entityClass = switch (this.config.sensitivity) {
            case BlockSetType.PressurePlateSensitivity.EVERYTHING -> Entity.class;
            case BlockSetType.PressurePlateSensitivity.MOBS -> LivingEntity.class;
        };
        return getEntityCount(level, AABB.move(pos), entityClass) > 0 ? 15 : 0;
    }

    static int getEntityCount(Level level, AABB entityDetectionBox, Class<? extends Entity> entityClass) {
        return level.getEntitiesOfClass(entityClass, entityDetectionBox, EntitySelector.NO_SPECTATORS.and(e -> !e.isIgnoringBlockTriggers())).size();
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PressurePlateBlock.POWERED);
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return BasePressurePlateBlock.canSupportRigidBlock(level, below) || BasePressurePlateBlock.canSupportCenter(level, below, Direction.UP);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int signal = this.getSignalForState(state);
        if (signal > 0) {
            this.checkPressed(null, level, pos, state, signal);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        BlockBehaviour.super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
    }

    @Override
    public @Nullable DamageSource getFallDamageSource(Entity entity) {
        return BlockBehaviour.super.getFallDamageSource(entity);
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        BlockBehaviour.super.onLand(level, blockPos, blockState, blockState2, fallingBlockEntity);
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        BlockBehaviour.super.onBrokenAfterFall(level, blockPos, fallingBlockEntity);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        if (level.isClientSide()) {
            return;
        }
        int signal = this.getSignalForState(blockState);
        if (signal == 0) {
            this.checkPressed(entity, level, blockPos, blockState, signal);
        }
    }

    private void checkPressed(Entity sourceEntity, Level level, BlockPos pos, BlockState state, int oldSignal) {
        int signal = this.getSignalStrength(level, pos);
        boolean wasPressed = oldSignal > 0;
        boolean isPressed = signal > 0;
        if (oldSignal != signal) {
            BlockState newState = this.setSignalForState(state, signal);
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos, state.getBlock());
            level.setBlocksDirty(pos, state, newState);
        }
        if (!isPressed && wasPressed) {
            level.playSound(null, pos, SoundEvent.createVariableRangeEvent(this.config.pressurePlateClickOff), SoundSource.BLOCKS);
            level.gameEvent(sourceEntity, GameEvent.BLOCK_DEACTIVATE, pos);
        } else if (isPressed && !wasPressed) {
            level.playSound(null, pos, SoundEvent.createVariableRangeEvent(this.config.pressurePlateClickOn), SoundSource.BLOCKS);
            level.gameEvent(sourceEntity, GameEvent.BLOCK_ACTIVATE, pos);
        }

        if (isPressed) {
            level.scheduleTick(new BlockPos(pos), state.getBlock(), this.config.pressedTime);
        }
    }

    public static class Config {
        public Identifier pressurePlateClickOff = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF.location();
        public Identifier pressurePlateClickOn = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON.location();
        public BlockSetType.PressurePlateSensitivity sensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;

        public int pressedTime = 20;
    }
}