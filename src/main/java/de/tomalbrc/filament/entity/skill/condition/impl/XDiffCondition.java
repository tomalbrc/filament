package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class XDiffCondition implements Condition {
    private final double min, max;

    XDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        double d = Math.abs(target.getPosition().x() - ctx.caster().getX());
        return d >= min && d <= max;
    }
}
