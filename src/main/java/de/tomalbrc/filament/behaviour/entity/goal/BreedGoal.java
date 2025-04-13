package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * BreedGoal
 */
public class BreedGoal implements EntityBehaviour<BreedGoal.Config> {
    private final Config config;

    public BreedGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new BreedGoalImpl(mob, config.speedModifier));
    }

    @Override
    @NotNull
    public BreedGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
    }

    public class BreedGoalImpl extends Goal {
        private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(8.0F).ignoreLineOfSight();
        protected final FilamentMob animal;
        protected final ServerLevel level;
        @Nullable
        protected FilamentMob partner;
        private int loveTime;
        private final double speedModifier;

        public BreedGoalImpl(FilamentMob filamentMob, double d) {
            this.animal = filamentMob;
            this.level = getServerLevel(filamentMob);
            this.speedModifier = d;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            if (!this.animal.isInLove()) {
                return false;
            } else {
                this.partner = this.getFreePartner();
                return this.partner != null;
            }
        }

        public boolean canContinueToUse() {
            return this.partner.isAlive() && this.partner.isInLove() && this.loveTime < 60 && !this.partner.isPanicking();
        }

        public void stop() {
            this.partner = null;
            this.loveTime = 0;
        }

        public void tick() {
            this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
            this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
            ++this.loveTime;
            if (this.loveTime >= this.adjustedTickDelay(60) && this.animal.distanceToSqr(this.partner) < (double)9.0F) {
                this.breed();
            }

        }

        @Nullable
        private FilamentMob getFreePartner() {
            List<? extends FilamentMob> list = this.level.getNearbyEntities(FilamentMob.class, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(8.0F));
            double dist = Double.MAX_VALUE;
            FilamentMob mob = null;

            for (FilamentMob mob2 : list) {
                if (this.animal.canMate(mob2) && !mob2.isPanicking() && this.animal.distanceToSqr(mob2) < dist) {
                    mob = mob2;
                    dist = this.animal.distanceToSqr(mob2);
                }
            }

            return mob;
        }

        protected void breed() {
            this.animal.spawnChildFromBreeding(this.level, this.partner);
        }
    }

}