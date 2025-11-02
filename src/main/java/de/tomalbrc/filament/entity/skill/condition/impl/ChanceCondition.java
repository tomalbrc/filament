package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class ChanceCondition implements Condition {
    private final double chance;

    ChanceCondition(double chance) {
        this.chance = chance;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.caster().getRandom().nextDouble() <= chance;
    }
}