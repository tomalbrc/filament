package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Follow any mob
 */
public class FollowMobGoal implements EntityBehaviour<FollowMobGoal.Config> {
    private final FollowMobGoal.Config config;

    public FollowMobGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new FollowMobGoalImpl(mob, config.speedModifier, config.stopDistance, config.areaSize));
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        float stopDistance;
        float areaSize;
    }

    public static class FollowMobGoalImpl extends Goal {
        private final Mob mob;
        private final Predicate<Mob> followPredicate;
        @Nullable
        private Mob followingMob;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private float oldWaterCost;
        private final float areaSize;

        public FollowMobGoalImpl(Mob mob, double speedModifier, float stopDistance, float areaSize) {
            this.mob = mob;
            this.followPredicate = (mob2) -> mob2 != null && mob.getType() != mob2.getType();
            this.speedModifier = speedModifier;
            this.navigation = mob.getNavigation();
            this.stopDistance = stopDistance;
            this.areaSize = areaSize;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
            }
        }

        public boolean canUse() {
            List<Mob> list = this.mob.level().getEntitiesOfClass(Mob.class, this.mob.getBoundingBox().inflate(this.areaSize), this.followPredicate);
            if (!list.isEmpty()) {
                for(Mob mob : list) {
                    if (!mob.isInvisible()) {
                        this.followingMob = mob;
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean canContinueToUse() {
            return this.followingMob != null && !this.navigation.isDone() && this.mob.distanceToSqr(this.followingMob) > (double)(this.stopDistance * this.stopDistance);
        }

        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
            this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        public void stop() {
            this.followingMob = null;
            this.navigation.stop();
            this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        public void tick() {
            if (this.followingMob != null && !this.mob.isLeashed()) {
                this.mob.getLookControl().setLookAt(this.followingMob, 10.0F, (float)this.mob.getMaxHeadXRot());
                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = this.adjustedTickDelay(10);
                    double xDelta = this.mob.getX() - this.followingMob.getX();
                    double yDelta = this.mob.getY() - this.followingMob.getY();
                    double zDelta = this.mob.getZ() - this.followingMob.getZ();
                    double len = xDelta * xDelta + yDelta * yDelta + zDelta * zDelta;
                    if (!(len <= (double)(this.stopDistance * this.stopDistance))) {
                        this.navigation.moveTo(this.followingMob, this.speedModifier);
                    } else {
                        this.navigation.stop();
                        LookControl lookControl = this.followingMob.getLookControl();
                        if (len <= (double)this.stopDistance || lookControl.getWantedX() == this.mob.getX() && lookControl.getWantedY() == this.mob.getY() && lookControl.getWantedZ() == this.mob.getZ()) {
                            double xd = this.followingMob.getX() - this.mob.getX();
                            double zd = this.followingMob.getZ() - this.mob.getZ();
                            this.navigation.moveTo(this.mob.getX() - xd, this.mob.getY(), this.mob.getZ() - zd, this.speedModifier);
                        }

                    }
                }
            }
        }
    }
}
