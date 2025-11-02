package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;

public class EntityTypeCondition implements Condition {
    private final ResourceLocation type;

    EntityTypeCondition(ResourceLocation t) {
        this.type = t;
    }

    public boolean test(SkillTree ctx, Target target) {
        return target.getEntity().getType().builtInRegistryHolder().key().location().equals(type);
    }
}