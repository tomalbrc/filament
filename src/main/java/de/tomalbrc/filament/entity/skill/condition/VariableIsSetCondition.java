package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class VariableIsSetCondition implements Condition {
    private final String var;

    VariableIsSetCondition(String var) {
        this.var = var;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.vars().containsKey(var);
    }
}
