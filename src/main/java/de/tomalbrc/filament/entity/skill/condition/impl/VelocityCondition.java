package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class VelocityCondition implements Condition {
    private final double min, max;

    VelocityCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        double v = target.getEntity().getDeltaMovement().length();
        return v >= min && v <= max;
    }
}
