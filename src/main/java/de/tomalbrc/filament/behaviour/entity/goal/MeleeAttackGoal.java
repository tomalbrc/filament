package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * MeleeAttackGoal
 */
public class MeleeAttackGoal implements EntityBehaviour<MeleeAttackGoal.Config> {
    private final Config config;

    public MeleeAttackGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(mob, config.speedModifier, config.followingTargetEvenIfNotSeen));
    }

    @Override
    @NotNull
    public MeleeAttackGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        boolean followingTargetEvenIfNotSeen;
    }
}