package de.tomalbrc.filament.behaviour.entity.target;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * DefendVillageGoal
 */
public class DefendVillageGoal implements EntityBehaviour<DefendVillageGoal.Config> {
    private final Config config;

    public DefendVillageGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getTargetSelector().addGoal(config.priority, new DefendVillageTargetGoal(mob));
    }

    @Override
    @NotNull
    public DefendVillageGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
    }

    public static class DefendVillageTargetGoal extends TargetGoal {
        private final PathfinderMob pathfinderMob;
        @Nullable
        private LivingEntity potentialTarget;
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range((double)64.0F);

        public DefendVillageTargetGoal(PathfinderMob pathfinderMob) {
            super(pathfinderMob, false, true);
            this.pathfinderMob = pathfinderMob;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        public boolean canUse() {
            AABB aABB = this.pathfinderMob.getBoundingBox().inflate(10.0F, 8.0F, 10.0F);
            ServerLevel serverLevel = getServerLevel(this.pathfinderMob);
            List<? extends LivingEntity> list = serverLevel.getNearbyEntities(Villager.class, this.attackTargeting, this.pathfinderMob, aABB);
            List<Player> list2 = serverLevel.getNearbyPlayers(this.attackTargeting, this.pathfinderMob, aABB);

            for(LivingEntity livingEntity : list) {
                Villager villager = (Villager)livingEntity;
                for(Player player : list2) {
                    int i = villager.getPlayerReputation(player);
                    if (i <= -100) {
                        this.potentialTarget = player;
                    }
                }
            }

            if (this.potentialTarget == null) {
                return false;
            } else {
                LivingEntity var12 = this.potentialTarget;
                if (var12 instanceof Player player2) {
                    return !player2.isSpectator() && !player2.isCreative();
                }

                return true;
            }
        }

        public void start() {
            this.pathfinderMob.setTarget(this.potentialTarget);
            super.start();
        }
    }

}
