package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class FallSpeedCondition implements Condition {
    private final double min, max;

    FallSpeedCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double v = Math.abs(target.getEntity().getDeltaMovement().y);
        return v >= min && v <= max;
    }
}