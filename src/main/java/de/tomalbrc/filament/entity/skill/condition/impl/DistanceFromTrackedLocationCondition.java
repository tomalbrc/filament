package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class DistanceFromTrackedLocationCondition implements Condition {
    private final String var;
    private final double min, max;

    DistanceFromTrackedLocationCondition(String var, double min, double max) {
        this.var = var;
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        Variable o = ctx.vars().get(var);
        double d = o.asVec3().distanceToSqr(ctx.caster.position());
        return d >= min * min && d <= max * max;
    }
}