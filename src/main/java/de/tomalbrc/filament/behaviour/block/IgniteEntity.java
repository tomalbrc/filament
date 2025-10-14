package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviourWithEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
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
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        insideBlockEffectApplier.apply(InsideBlockEffectType.FIRE_IGNITE);
        insideBlockEffectApplier.runAfter(InsideBlockEffectType.FIRE_IGNITE, entityx -> entityx.hurt(entityx.level().damageSources().inFire(), this.config.fireDamage));
    }

    public static class Config {
        float fireDamage = 1;
    }
}