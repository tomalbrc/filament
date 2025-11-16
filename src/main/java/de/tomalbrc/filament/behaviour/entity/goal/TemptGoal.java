package de.tomalbrc.filament.behaviour.entity.goal;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * TemptGoal
 */
public class TemptGoal implements EntityBehaviour<TemptGoal.Config> {
    private final Config config;

    public TemptGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        mob.getGoalSelector().addGoal(config.priority, new net.minecraft.world.entity.ai.goal.TemptGoal(mob, config.speedModifier, x -> {
            if (config.items != null) {
                for (ResourceLocation resourceLocation : config.items) {
                    if (x.is(BuiltInRegistries.ITEM.get(resourceLocation)))
                        return true;
                }
            }
            if (config.itemTags != null) {
                for (ResourceLocation resourceLocation : config.itemTags) {
                    if (x.is(TagKey.create(Registries.ITEM, resourceLocation)))
                        return true;
                }
            }
            return false;
        }, config.canScare));
    }

    @Override
    @NotNull
    public TemptGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        float speedModifier = 1.f;
        boolean canScare;
        Set<ResourceLocation> items;
        Set<ResourceLocation> itemTags;
    }
}