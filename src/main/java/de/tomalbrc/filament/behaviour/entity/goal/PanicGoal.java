package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * PanicGoal
 */
public class PanicGoal implements EntityBehaviour<PanicGoal.Config> {
    private final Config config;

    public PanicGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.PanicGoal(mob, config.speedModifier, TagKey.create(Registries.DAMAGE_TYPE, config.damageType)));
    }

    @Override
    @NotNull
    public PanicGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        Identifier damageType = DamageTypeTags.PANIC_CAUSES.location();
    }
}