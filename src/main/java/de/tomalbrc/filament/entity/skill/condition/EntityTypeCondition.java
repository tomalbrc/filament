package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;

class EntityTypeCondition implements Condition {
    private final ResourceLocation type;

    EntityTypeCondition(ResourceLocation t) {
        this.type = t;
    }

    public boolean test(SkillContext ctx, Target target) {
        return target.getEntity().getType().builtInRegistryHolder().key().location().equals(type);
    }
}