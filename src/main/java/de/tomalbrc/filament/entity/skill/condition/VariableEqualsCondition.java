package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

class VariableEqualsCondition implements Condition {
    private final String var;
    private final String value;

    VariableEqualsCondition(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public boolean test(SkillContext ctx, Target target) {
        return Objects.equals(ctx.vars().get(var).getRaw(), value);
    }
}
