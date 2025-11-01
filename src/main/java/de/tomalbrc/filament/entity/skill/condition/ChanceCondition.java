package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class ChanceCondition implements Condition {
    private final double chance;

    ChanceCondition(double chance) {
        this.chance = chance;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.caster().getRandom().nextDouble() <= chance;
    }
}