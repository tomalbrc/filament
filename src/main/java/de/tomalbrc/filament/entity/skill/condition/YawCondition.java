package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class YawCondition implements Condition {
    private final double min, max;

    YawCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double yaw = target.getEntity().getYRot();
        return yaw >= min && yaw <= max;
    }
}
