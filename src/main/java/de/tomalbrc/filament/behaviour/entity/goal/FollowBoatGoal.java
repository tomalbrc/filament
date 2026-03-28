package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.jetbrains.annotations.NotNull;

/**
 * FollowBoatGoal
 */
public class FollowBoatGoal implements EntityBehaviour<FollowBoatGoal.Config> {
    private final Config config;

    public FollowBoatGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        // TODO: 26.1 change
        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.FollowPlayerRiddenEntityGoal(mob, AbstractBoat.class));
        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.FollowPlayerRiddenEntityGoal(mob, AbstractNautilus.class));
    }

    @Override
    @NotNull
    public FollowBoatGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
    }
}