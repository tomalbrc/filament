package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

class LineOfSightCondition implements Condition {
    private final double range;

    LineOfSightCondition(double r) {
        this.range = r;
    }

    public boolean test(SkillContext ctx, Target target) {
        return Objects.requireNonNull(ctx.caster().asLivingEntity()).hasLineOfSight(target.getEntity()) && target.getEntity().distanceTo(ctx.caster()) <= range;
    }
}
