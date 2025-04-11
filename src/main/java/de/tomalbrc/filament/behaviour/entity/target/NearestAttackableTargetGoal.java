package de.tomalbrc.filament.behaviour.entity.target;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.behaviour.entity.EntityClassMapGenerator;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * NearestAttackableTargetGoal
 */
public class NearestAttackableTargetGoal implements EntityBehaviour<NearestAttackableTargetGoal.Config> {
    private final Config config;

    public NearestAttackableTargetGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getTargetSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(mob, (Class<? extends LivingEntity>) EntityClassMapGenerator.getEntityClass(config.target), config.randomInterval, config.mustSee, config.mustReach, this::check));
    }

    boolean check(LivingEntity livingEntity, ServerLevel serverLevel) {
        return (!config.ignoreBaby || livingEntity.isBaby()) && (!config.ignoreInWater || !livingEntity.isInWater());
    }

    @Override
    @NotNull
    public NearestAttackableTargetGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        ResourceLocation target;
        int randomInterval = 10;
        boolean mustSee = true;
        boolean mustReach = true;
        boolean ignoreBaby = false;
        boolean ignoreInWater = false;
    }
}
