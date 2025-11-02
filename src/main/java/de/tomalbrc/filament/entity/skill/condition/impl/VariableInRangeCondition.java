package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class VariableInRangeCondition implements Condition {
    private final String var;
    private final double min, max;

    VariableInRangeCondition(String var, double min, double max) {
        this.var = var;
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        Number v = ctx.vars().get(var).asNumber();
        double val = v.doubleValue();
        return val >= min && val <= max;
    }
}
