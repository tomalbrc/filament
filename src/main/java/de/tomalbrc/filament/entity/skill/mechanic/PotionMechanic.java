package de.tomalbrc.filament.entity.skill.mechanic;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import de.tomalbrc.filament.entity.skill.target.Targeter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class PotionMechanic implements Mechanic {
    private final ResourceLocation potionType;
    private final int duration;
    private final int level;
    private final Targeter targeter;

    public PotionMechanic(ResourceLocation potionType, int duration, int level, Targeter targeter) {
        this.potionType = potionType;
        this.duration = duration;
        this.level = level;
        this.targeter = targeter;
    }

    @Override
    public int execute(SkillContext context) {
        if (context.targets() != null) for (Target target : context.targets()) {
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
