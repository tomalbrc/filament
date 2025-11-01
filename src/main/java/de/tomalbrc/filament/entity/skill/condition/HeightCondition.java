package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class HeightCondition implements Condition {
    private final double min, max;

    HeightCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double y = target.getPosition().y();
        return y >= min && y <= max;
    }
}
