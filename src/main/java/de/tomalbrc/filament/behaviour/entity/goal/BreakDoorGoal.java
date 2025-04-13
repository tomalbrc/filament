package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * BreakDoorGoal
 */
public class BreakDoorGoal implements EntityBehaviour<BreakDoorGoal.Config> {
    private final Config config;

    public BreakDoorGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.BreakDoorGoal(mob, config.doorBreakTime, difficulty -> {
            if (config.validDifficulties != null) {
                return config.validDifficulties.contains(difficulty);
            }
            return true;
        }));
    }

    @Override
    @NotNull
    public BreakDoorGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        int doorBreakTime = 240;
        Set<Difficulty> validDifficulties;
    }
}