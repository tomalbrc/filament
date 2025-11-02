package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class MotionYCondition implements Condition {
    private final double min, max;

    MotionYCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        double v = target.getEntity().getDeltaMovement().y;
        return v >= min && v <= max;
    }
}
