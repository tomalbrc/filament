package de.tomalbrc.filament.behaviour.entity.target;

import de.tomalbrc.filament.api.behaviour.EntityBehaviour;
import de.tomalbrc.filament.behaviour.entity.EntityClassMapGenerator;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * HurtByTargetGoal
 */
public class HurtByTargetGoal implements EntityBehaviour<HurtByTargetGoal.Config> {
    private final Config config;

    public HurtByTargetGoal(Config config) {
        this.config = config;
    }

    @Override
    public void registerGoals(FilamentMob mob) {
        EntityBehaviour.super.registerGoals(mob);

        var size = config.ignoreFrom == null ? 0 : config.ignoreFrom.size();
        Class<?>[] classes = new Class[size];
        for (int i = 0; i < size; i++) {
            classes[i] = EntityClassMapGenerator.getEntityClass(config.ignoreFrom.get(i));
        }

        var goal = new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(mob, classes);
        if (config.alertOthers != null && !config.alertOthers.isEmpty()) {
            Class<?>[] classes2 = new Class[config.alertOthers.size()];
            for (int i = 0; i < config.alertOthers.size(); i++) {
                classes2[i] = EntityClassMapGenerator.getEntityClass(config.alertOthers.get(i));
            }
            goal = goal.setAlertOthers(classes2);
        }
        mob.getTargetSelector().addGoal(config.priority, goal);
    }

    @Override
    @NotNull
    public HurtByTargetGoal.Config getConfig() {
        return this.config;
    }

    public static class Config {
        int priority;
        List<ResourceLocation> ignoreFrom;
        List<ResourceLocation> alertOthers;
    }
}