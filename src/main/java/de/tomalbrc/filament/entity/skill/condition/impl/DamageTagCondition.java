package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;

public class DamageTagCondition implements Condition {
    private final ResourceLocation tag;

    DamageTagCondition(ResourceLocation t) {
        this.tag = t;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.vars().getOrDefault("damageTags", Variable.EMPTY).asSet().contains(tag);
    }
}
