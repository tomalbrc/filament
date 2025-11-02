package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class VariableContainsCondition implements Condition {
    private final String var;
    private final String value;

    VariableContainsCondition(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public boolean test(SkillTree ctx, Target target) {
        var v = ctx.vars().get(var);
        return v != null && v.getRaw().toString().contains(value);
    }
}
