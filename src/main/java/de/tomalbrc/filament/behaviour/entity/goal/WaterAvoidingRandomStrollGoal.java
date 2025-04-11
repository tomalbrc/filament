package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * Float goal
 */
public class WaterAvoidingRandomStrollGoal implements EntityBehaviour<WaterAvoidingRandomStrollGoal.Config> {
    private final Config config;

    public WaterAvoidingRandomStrollGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(mob, config.speedModifier, config.probability));
    }

    @Override
    @NotNull
    public WaterAvoidingRandomStrollGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        float probability = 0.001f;
    }
}