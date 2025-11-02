package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class VariableIsSetCondition implements Condition {
    private final String var;

    VariableIsSetCondition(String var) {
        this.var = var;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.vars().containsKey(var);
    }
}
