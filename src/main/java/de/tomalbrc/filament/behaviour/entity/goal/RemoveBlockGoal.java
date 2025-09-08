package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * RemoveBlockGoal
 */
public class RemoveBlockGoal implements EntityBehaviour<RemoveBlockGoal.Config> {
    private final Config config;

    public RemoveBlockGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.RemoveBlockGoal(config.block.getBlock(), mob, config.speedModifier, config.verticalSearchRange));
    }

    @Override
    @NotNull
    public RemoveBlockGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        BlockState block;
        float speedModifier = 1.f;
        int verticalSearchRange = 3;
    }
}
