package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class IgniteEntity implements BlockBehaviourWithEntity<IgniteEntity.Config> {
    private final Config config;

    public IgniteEntity(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public IgniteEntity.Config getConfig() {
        return this.config;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.igniteForSeconds(8.0F);
            }
        }

        entity.hurt(level.damageSources().inFire(), this.config.fireDamage);
    }

    public static class Config {
        float fireDamage = 1;
    }
}