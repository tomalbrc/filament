package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class VariableContainsCondition implements Condition {
    private final String var;
    private final String value;

    VariableContainsCondition(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public boolean test(SkillContext ctx, Target target) {
        var v = ctx.vars().get(var);
        return v != null && v.getRaw().toString().contains(value);
    }
}
