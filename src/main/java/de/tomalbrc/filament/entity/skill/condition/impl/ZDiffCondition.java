package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class ZDiffCondition implements Condition {
    private final double min, max;

    ZDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        double d = Math.abs(target.getPosition().z() - ctx.caster().getZ());
        return d >= min && d <= max;
    }
}
