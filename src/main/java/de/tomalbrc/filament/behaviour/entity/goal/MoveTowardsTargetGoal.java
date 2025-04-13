package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * MoveTowardsTargetGoal
 */
public class MoveTowardsTargetGoal implements EntityBehaviour<MoveTowardsTargetGoal.Config> {
    private final Config config;

    public MoveTowardsTargetGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal(mob, config.speedModifier, config.within));
    }

    @Override
    @NotNull
    public MoveTowardsTargetGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        float within;
    }
}