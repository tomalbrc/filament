package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class ZDiffCondition implements Condition {
    private final double min, max;

    ZDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        double d = Math.abs(target.getPosition().z() - ctx.caster().getZ());
        return d >= min && d <= max;
    }
}
