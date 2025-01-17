package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FallingBlock implements BlockBehaviour<FallingBlock.Config> {
    private final Config config;

    public FallingBlock(Config config) {
        this.config = config;
    }

    @Override
    public @NotNull FallingBlock.Config getConfig() {
        return config;
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        level.scheduleTick(blockPos, blockState.getBlock(), config.delayAfterPlace.getValue(blockState));
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        scheduledTickAccess.scheduleTick(blockPos, blockState.getBlock(), config.delayAfterPlace.getValue(blockState));
        return BlockBehaviour.super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (net.minecraft.world.level.block.FallingBlock.isFree(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinY()) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, blockPos, blockState);

            if (config.disableDrops.getValue(blockState))
                fallingBlockEntity.disableDrop();

            fallingBlockEntity.setSilent(true);
            this.falling(fallingBlockEntity);
        }
    }

    private void falling(FallingBlockEntity fallingBlockEntity) {
        if (config.heavy.getValue(fallingBlockEntity.getBlockState()))
            fallingBlockEntity.setHurtsEntities(config.damagePerDistance.getValue(fallingBlockEntity.getBlockState()), config.maxDamage.getValue(fallingBlockEntity.getBlockState()));
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (!config.silent.getValue(blockState)) {
            level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.landSound.getValue(blockState2)), SoundSource.BLOCKS);
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        var bs = level.getBlockState(blockPos);
        if (!config.silent.getValue(bs)) {
            level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.breakSound.getValue(bs)), SoundSource.BLOCKS);
        }
    }

    public static class Config {
        BlockStateMappedProperty<Integer> delayAfterPlace = BlockStateMappedProperty.of(2);
        BlockStateMappedProperty<Boolean> heavy = BlockStateMappedProperty.of(false);
        public BlockStateMappedProperty<Float> damagePerDistance = BlockStateMappedProperty.of(2.f);
        public BlockStateMappedProperty<Integer> maxDamage = BlockStateMappedProperty.of(40);
        BlockStateMappedProperty<Boolean> disableDrops = BlockStateMappedProperty.of(false);
        BlockStateMappedProperty<Boolean> silent = BlockStateMappedProperty.of(false);
        BlockStateMappedProperty<ResourceLocation> breakSound = BlockStateMappedProperty.of(SoundEvents.ANVIL_BREAK.location());
        BlockStateMappedProperty<ResourceLocation> landSound = BlockStateMappedProperty.of(SoundEvents.ANVIL_LAND.location());
        public BlockStateMappedProperty<Boolean> canBeDamaged = BlockStateMappedProperty.of(false);
        public BlockStateMappedProperty<ResourceLocation> damagedBlock = null;
        public BlockStateMappedProperty<Float> baseBreakChance = BlockStateMappedProperty.of(0.05f);
        public BlockStateMappedProperty<Float> breakChancePerDistance = BlockStateMappedProperty.of(0.05f);
    }
}
