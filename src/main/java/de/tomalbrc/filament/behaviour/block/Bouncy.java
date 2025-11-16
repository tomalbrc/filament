package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Bouncy blocks
 */
public class Bouncy implements BlockBehaviour<Bouncy.Config> {
    private final Config config;

    public Bouncy(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Bouncy.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        if (!entity.isSuppressingBounce()) {
            entity.causeFallDamage((float) d, 0.0F, level.damageSources().fall());
        }

        return true;
    }

    @Override
    public boolean updateEntityMovementAfterFallOn(BlockGetter blockGetter, Entity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (deltaMovement.y < -0.4 && !entity.isSuppressingBounce()) {
            if (config.bounciness > 0) setVel(entity, new Vec3(deltaMovement.x, Mth.clamp(0, -deltaMovement.y * config.bounciness, config.max), deltaMovement.z));
        } else if (deltaMovement.y < -0.08 && !entity.isSuppressingBounce()) {
            double rebounce = entity instanceof LivingEntity ? 1.0 : 0.8;
            setVel(entity, new Vec3(deltaMovement.x, -deltaMovement.y * rebounce, deltaMovement.z));
        } else {
            setVel(entity, deltaMovement.multiply(1.0, 0.0, 1.0));
        }

        return true;
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        double absY = Math.abs(entity.getDeltaMovement().y);
        if (absY < 0.1 && !entity.isSteppingCarefully()) {
            setVel(entity, entity.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }
    }

    private void setVel(Entity entity,Vec3 vel) {
        entity.setDeltaMovement(vel);
        entity.hurtMarked = true;
    }

    public static class Config {
        public double bounciness = 0;
        public double max = 10;
    }
}