package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * MoveThroughVillageGoal
 */
public class MoveThroughVillageGoal implements EntityBehaviour<MoveThroughVillageGoal.Config> {
    private final Config config;

    public MoveThroughVillageGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal(mob, config.speedModifier, config.onlyAtNight, config.distanceToPoi, () -> config.canDealWithDoors));
    }

    @Override
    @NotNull
    public MoveThroughVillageGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        boolean onlyAtNight = true;
        int distanceToPoi = 8;
        boolean canDealWithDoors;
    }
}