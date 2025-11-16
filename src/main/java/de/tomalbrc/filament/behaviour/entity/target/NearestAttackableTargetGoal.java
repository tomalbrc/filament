package de.tomalbrc.filament.behaviour.entity.target;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * NearestAttackableTargetGoal
 */
public class NearestAttackableTargetGoal implements EntityBehaviour<NearestAttackableTargetGoal.Config> {
    private final Config config;

    public NearestAttackableTargetGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getTargetSelector().addGoal(config.priority, new NearestAttackableTargetGoalImpl(mob, BuiltInRegistries.ENTITY_TYPE.get(config.target), config.randomInterval, config.mustSee, config.mustReach, this::check));
    }

    boolean check(LivingEntity livingEntity) {
        return (!config.ignoreBaby || livingEntity.isBaby()) && (!config.ignoreInWater || !livingEntity.isInWater());
    }

    @Override
    @NotNull
    public NearestAttackableTargetGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        ResourceLocation target;
        int randomInterval = 10;
        boolean mustSee = true;
        boolean mustReach = true;
        boolean ignoreBaby = false;
        boolean ignoreInWater = false;
    }

    public static class NearestAttackableTargetGoalImpl extends TargetGoal {
        protected final EntityType<?> targetType;
        protected final int randomInterval;
        @Nullable
        protected LivingEntity target;
        protected TargetingConditions targetConditions;

        public NearestAttackableTargetGoalImpl(Mob mob, EntityType<?> entityType, int interval, boolean mustSee, boolean mustReach, Predicate<LivingEntity> selector) {
            super(mob, mustSee, mustReach);
            this.targetType = entityType;
            this.randomInterval = reducedTickDelay(interval);
            this.setFlags(EnumSet.of(Flag.TARGET));
            this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(selector);
        }

        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                this.findTarget();
                return this.target != null;
            }
        }

        protected AABB getTargetSearchArea(double d) {
            return this.mob.getBoundingBox().inflate(d, d, d);
        }

        protected void findTarget() {
            ServerLevel serverLevel = (ServerLevel) this.mob.level();
            if (this.targetType != EntityType.PLAYER) {
                this.target = serverLevel.getNearestEntity(this.mob.level().getEntitiesOfClass(Mob.class, this.getTargetSearchArea(this.getFollowDistance()), (livingEntity) -> livingEntity.getType() == this.targetType), this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.target = serverLevel.getNearestPlayer(this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }

        }

        public void start() {
            this.mob.setTarget(this.target);
            super.start();
        }

        public void setTarget(@Nullable LivingEntity livingEntity) {
            this.target = livingEntity;
        }

        private TargetingConditions getTargetConditions() {
            return this.targetConditions.range(this.getFollowDistance());
        }
    }
}
