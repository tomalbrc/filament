package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import org.jetbrains.annotations.NotNull;

/**
 * OpenDoorGoal
 */
public class OpenDoorGoal implements EntityBehaviour<OpenDoorGoal.Config> {
    private final Config config;

    public OpenDoorGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.OpenDoorGoal(mob, config.closeDoor));
    }

    @Override
    @NotNull
    public OpenDoorGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        boolean closeDoor = true;
    }
}