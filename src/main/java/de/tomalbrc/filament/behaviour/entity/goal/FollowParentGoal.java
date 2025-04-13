package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Follow parent
 */
public class FollowParentGoal implements EntityBehaviour<FollowParentGoal.Config> {
    private final FollowParentGoal.Config config;

    public FollowParentGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new FollowParentGoalImpl(mob, config.speedModifier));
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
    }

    public static class FollowParentGoalImpl extends Goal {
        private final FilamentMob mob;
        @Nullable
        private Animal parent;
        private final double speedModifier;
        private int timeToRecalcPath;

        public FollowParentGoalImpl(FilamentMob mob, double d) {
            this.mob = mob;
            this.speedModifier = d;
        }

        public boolean canUse() {
            if (this.mob.getAge() >= 0) {
                return false;
            } else {
                List<? extends Animal> list = this.mob.level().getEntitiesOfClass(Animal.class, this.mob.getBoundingBox().inflate(8.0F, 4.0F, 8.0F), x -> x.getType() == mob.getType());
                Animal animal = null;
                double d = Double.MAX_VALUE;

                for(Animal animal2 : list) {
                    if (animal2.getAge() >= 0) {
                        double e = this.mob.distanceToSqr(animal2);
                        if (!(e > d)) {
                            d = e;
                            animal = animal2;
                        }
                    }
                }

                if (animal == null) {
                    return false;
                } else if (d < (double)9.0F) {
                    return false;
                } else {
                    this.parent = animal;
                    return true;
                }
            }
        }

        public boolean canContinueToUse() {
            if (this.mob.getAge() >= 0) {
                return false;
            } else if (!this.parent.isAlive()) {
                return false;
            } else {
                double d = this.mob.distanceToSqr(this.parent);
                return !(d < (double)9.0F) && !(d > (double)256.0F);
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
        }

        public void stop() {
            this.parent = null;
        }

        public void tick() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                this.mob.getNavigation().moveTo(this.parent, this.speedModifier);
            }
        }
    }
}
