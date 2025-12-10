package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * LookAtMobGoal
 */
public class LookAtMobGoal implements EntityBehaviour<LookAtMobGoal.Config> {
    private final Config config;

    public LookAtMobGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new LookAtMobGoalImpl(mob, BuiltInRegistries.ENTITY_TYPE.getValue(config.target), config.lookDistance, config.probability, config.onlyHorizontal));
    }

    @Override
    @NotNull
    public LookAtMobGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        Identifier target;
        float lookDistance;
        float probability;
        boolean onlyHorizontal;
    }

    public static class LookAtMobGoalImpl extends Goal {
        protected final Mob mob;
        @Nullable protected Entity lookAt;
        protected final float lookDistance;
        private int lookTime;
        protected final float probability;
        private final boolean onlyHorizontal;
        protected final EntityType<?> lookAtType;
        protected final TargetingConditions lookAtContext;

        public LookAtMobGoalImpl(Mob mob, EntityType<?> entityType, float f, float g, boolean bl) {
            this.mob = mob;
            this.lookAtType = entityType;
            this.lookDistance = f;
            this.probability = g;
            this.onlyHorizontal = bl;
            this.setFlags(EnumSet.of(Flag.LOOK));
            if (entityType == EntityType.PLAYER) {
                Predicate<Entity> predicate = EntitySelector.notRiding(mob);
                this.lookAtContext = TargetingConditions.forNonCombat().range(f).selector((livingEntity, serverLevel) -> predicate.test(livingEntity));
            } else {
                this.lookAtContext = TargetingConditions.forNonCombat().range(f);
            }

        }

        public boolean canUse() {
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                return false;
            } else {
                if (this.mob.getTarget() != null) {
                    this.lookAt = this.mob.getTarget();
                }

                ServerLevel serverLevel = getServerLevel(this.mob);
                if (this.lookAtType == EntityType.PLAYER) {
                    this.lookAt = serverLevel.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                } else {
                    this.lookAt = serverLevel.getNearestEntity(this.mob.level().getEntitiesOfClass(Mob.class, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0F, this.lookDistance), (livingEntity) -> livingEntity.getType() == lookAtType), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                }

                return this.lookAt != null;
            }
        }

        public boolean canContinueToUse() {
            if (!this.lookAt.isAlive()) {
                return false;
            } else if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
                return false;
            } else {
                return this.lookTime > 0;
            }
        }

        public void start() {
            this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
        }

        public void stop() {
            this.lookAt = null;
        }

        public void tick() {
            if (this.lookAt.isAlive()) {
                double d = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
                this.mob.getLookControl().setLookAt(this.lookAt.getX(), d, this.lookAt.getZ());
                --this.lookTime;
            }
        }
    }
}