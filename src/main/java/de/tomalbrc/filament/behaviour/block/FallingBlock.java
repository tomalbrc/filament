package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
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
        level.scheduleTick(blockPos, blockState.getBlock(), config.delayAfterPlace);
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        scheduledTickAccess.scheduleTick(blockPos, blockState.getBlock(), config.delayAfterPlace);
        return BlockBehaviour.super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (net.minecraft.world.level.block.FallingBlock.isFree(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinY()) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, blockPos, blockState);

            if (config.disableDrops)
                fallingBlockEntity.disableDrop();

            fallingBlockEntity.setSilent(true);
            this.falling(fallingBlockEntity);
        }
    }

    private void falling(FallingBlockEntity fallingBlockEntity) {
        if (config.heavy)
            fallingBlockEntity.setHurtsEntities(config.damagePerDistance, config.maxDamage);
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (!config.silent) {
            level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.landSound), SoundSource.BLOCKS);
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        if (!config.silent) {
            level.playSound(null, blockPos, SoundEvent.createVariableRangeEvent(config.breakSound), SoundSource.BLOCKS);
        }
    }

    public static class Config {
        int delayAfterPlace = 2;
        boolean heavy = false;
        public float damagePerDistance = 2.f;
        public int maxDamage = 40;
        boolean disableDrops = false;
        boolean silent = false;
        ResourceLocation breakSound = SoundEvents.ANVIL_BREAK.location();
        ResourceLocation landSound = SoundEvents.ANVIL_LAND.location();
        public boolean canBeDamaged = false;
        public ResourceLocation damagedBlock = null;
        public float baseBreakChance = 0.05f;
        public float breakChancePerDistance = 0.05f;
    }
}
