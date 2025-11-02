package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

public class VariableEqualsCondition implements Condition {
    private final String var;
    private final String value;

    VariableEqualsCondition(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public boolean test(SkillTree ctx, Target target) {
        return Objects.equals(ctx.vars().get(var).getRaw(), value);
    }
}
