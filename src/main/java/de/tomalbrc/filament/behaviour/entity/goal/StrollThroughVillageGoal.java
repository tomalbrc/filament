package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * StrollThroughVillageGoal
 */
public class StrollThroughVillageGoal implements EntityBehaviour<StrollThroughVillageGoal.Config> {
    private final Config config;

    public StrollThroughVillageGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal(mob, config.interval));
    }

    @Override
    @NotNull
    public StrollThroughVillageGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        int interval = 100;
    }
}