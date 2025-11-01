package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class HealthCondition implements Condition {
    private final double min, max;

    HealthCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double h = target.getEntity().asLivingEntity().getHealth();
        return h >= min && h <= max;
    }
}
