package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

class EntityTypeCondition implements Condition {
    private final ResourceLocation type;

    EntityTypeCondition(ResourceLocation t) {
        this.type = t;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return target.getType().builtInRegistryHolder().key().location().equals(type);
    }
}