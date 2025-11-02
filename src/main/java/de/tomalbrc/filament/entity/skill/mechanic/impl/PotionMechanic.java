package de.tomalbrc.filament.entity.skill.mechanic.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanic;
import de.tomalbrc.filament.entity.skill.mechanic.Mechanics;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class PotionMechanic implements Mechanic {
    private final ResourceLocation potionType;
    private final int duration;
    private final int level;

    public PotionMechanic(ResourceLocation potionType, int duration, int level) {
        this.potionType = potionType;
        this.duration = duration;
        this.level = level;
    }

    @Override
    public int execute(SkillTree context) {
        if (context.getCurrentTargets() != null) for (Target target : context.getCurrentTargets()) {
            if (target.getEntity() instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.get(potionType).orElseThrow(), duration, level));
            }
        }

        return 0;
    }

    @Override
    public ResourceLocation id() {
        return Mechanics.POTION;
    }
}
