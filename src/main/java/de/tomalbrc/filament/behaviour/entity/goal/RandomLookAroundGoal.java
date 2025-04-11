package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * RandomLookAroundGoal
 */
public class RandomLookAroundGoal implements EntityBehaviour<RandomLookAroundGoal.Config> {
    private final Config config;

    public RandomLookAroundGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(mob));
    }

    @Override
    @NotNull
    public RandomLookAroundGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
    }
}