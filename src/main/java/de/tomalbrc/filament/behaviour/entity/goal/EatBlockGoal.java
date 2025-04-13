package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * EatBlockGoal
 */
public class EatBlockGoal implements EntityBehaviour<EatBlockGoal.Config> {
    private final Config config;

    public EatBlockGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.EatBlockGoal(mob));
    }

    @Override
    @NotNull
    public EatBlockGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
    }
}