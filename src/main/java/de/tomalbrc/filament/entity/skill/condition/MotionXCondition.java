package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class MotionXCondition implements Condition {
    private final double min, max;

    MotionXCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double v = target.getEntity().getDeltaMovement().x;
        return v >= min && v <= max;
    }
}
