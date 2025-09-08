package de.tomalbrc.filament.behaviour.entity.target;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * HurtByTargetGoal
 */
public class HurtByTargetGoal implements EntityBehaviour<HurtByTargetGoal.Config> {
    private final Config config;

    public HurtByTargetGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        var size = config.ignoreFrom == null ? 0 : config.ignoreFrom.size();
        EntityType<?>[] classes = new EntityType[size];
        for (int i = 0; i < size; i++) {
            classes[i] = BuiltInRegistries.ENTITY_TYPE.getValue(config.ignoreFrom.get(i));
        }

        var goal = new HurtByTargetGoalImpl(mob, classes);
        if (config.alertOthers != null && !config.alertOthers.isEmpty()) {
            EntityType<?>[] classes2 = new EntityType[config.alertOthers.size()];
            for (int i = 0; i < config.alertOthers.size(); i++) {
                classes2[i] = BuiltInRegistries.ENTITY_TYPE.getValue(config.alertOthers.get(i));
            }
            goal = goal.setAlertOthers(classes2);
        }
        mob.getTargetSelector().addGoal(config.priority, goal);
    }

    @Override
    @NotNull
    public HurtByTargetGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        List<ResourceLocation> ignoreFrom;
        List<ResourceLocation> alertOthers;
    }

    public static class HurtByTargetGoalImpl extends TargetGoal {
        private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        private static final int ALERT_RANGE_Y = 10;
        private boolean alertSameType;
        private int timestamp;
        private final EntityType<?>[] toIgnoreDamage;
        @Nullable
        private EntityType<?>[] toIgnoreAlert;

        public HurtByTargetGoalImpl(PathfinderMob pathfinderMob, EntityType<?>... classs) {
            super(pathfinderMob, true);
            this.toIgnoreDamage = classs;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        public boolean canUse() {
            int i = this.mob.getLastHurtByMobTimestamp();
            LivingEntity livingEntity = this.mob.getLastHurtByMob();
            if (i != this.timestamp && livingEntity != null) {
                if (livingEntity.getType() == EntityType.PLAYER && getServerLevel(this.mob).getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                    return false;
                } else {
                    for(EntityType<?> type : this.toIgnoreDamage) {
                        if (type == livingEntity.getType()) {
                            return false;
                        }
                    }

                    return this.canAttack(livingEntity, HURT_BY_TARGETING);
                }
            } else {
                return false;
            }
        }

        public HurtByTargetGoalImpl setAlertOthers(EntityType<?>... classs) {
            this.alertSameType = true;
            this.toIgnoreAlert = classs;
            return this;
        }

        public void start() {
            this.mob.setTarget(this.mob.getLastHurtByMob());
            this.targetMob = this.mob.getTarget();
            this.timestamp = this.mob.getLastHurtByMobTimestamp();
            this.unseenMemoryTicks = 300;
            if (this.alertSameType) {
                this.alertOthers();
            }

            super.start();
        }

        protected void alertOthers() {
            double followDistance = this.getFollowDistance();
            AABB aABB = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(followDistance, (double)10.0F, followDistance);
            List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aABB, EntitySelector.NO_SPECTATORS);
            Iterator<? extends Mob> iterator = list.iterator();

            while(true) {
                Mob mob;
                while(true) {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    mob = iterator.next();
                    if (this.mob != mob && mob.getTarget() == null && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)mob).getOwner()) && !mob.isAlliedTo(this.mob.getLastHurtByMob())) {
                        if (this.toIgnoreAlert == null) {
                            break;
                        }

                        boolean bl = false;

                        for(EntityType<?> type : this.toIgnoreAlert) {
                            if (mob.getType() == type) {
                                bl = true;
                                break;
                            }
                        }

                        if (!bl) {
                            break;
                        }
                    }
                }

                this.alertOther(mob, this.mob.getLastHurtByMob());
            }
        }

        protected void alertOther(Mob mob, LivingEntity livingEntity) {
            mob.setTarget(livingEntity);
        }
    }
}