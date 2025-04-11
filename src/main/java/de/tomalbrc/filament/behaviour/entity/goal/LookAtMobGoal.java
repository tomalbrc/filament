package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.behaviour.entity.EntityClassMapGenerator;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import org.jetbrains.annotations.NotNull;

/**
 * LookAtMobGoal
 */
public class LookAtMobGoal implements EntityBehaviour<LookAtMobGoal.Config> {
    private final Config config;

    public LookAtMobGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new LookAtPlayerGoal(mob, (Class<? extends LivingEntity>) EntityClassMapGenerator.getEntityClass(config.target), config.lookDistance, config.probability, config.onlyHorizontal));
    }

    @Override
    @NotNull
    public LookAtMobGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        ResourceLocation target;
        float lookDistance;
        float probability;
        boolean onlyHorizontal;
    }
}