package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

public class LineOfSightCondition implements Condition {
    private final double range;

    LineOfSightCondition(double r) {
        this.range = r;
    }

    public boolean test(SkillTree ctx, Target target) {
        return Objects.requireNonNull(ctx.caster().asLivingEntity()).hasLineOfSight(target.getEntity()) && target.getEntity().distanceTo(ctx.caster()) <= range;
    }
}
