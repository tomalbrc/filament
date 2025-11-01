package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceLocation;

class DamageTagCondition implements Condition {
    private final ResourceLocation tag;

    DamageTagCondition(ResourceLocation t) {
        this.tag = t;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.vars().getOrDefault("damageTags", Variable.EMPTY).asSet().contains(tag);
    }
}
