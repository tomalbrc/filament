package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * RandomStrollGoal
 */
public class RandomStrollGoal implements EntityBehaviour<RandomStrollGoal.Config> {
    private final Config config;

    public RandomStrollGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(mob, config.speedModifier, config.interval, config.checkNoActionTime));
    }

    @Override
    @NotNull
    public RandomStrollGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        int interval = 240;
        boolean checkNoActionTime = false;
    }
}