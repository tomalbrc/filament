package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class MotionZCondition implements Condition {
    private final double min, max;

    MotionZCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double v = target.getEntity().getDeltaMovement().z;
        return v >= min && v <= max;
    }
}
