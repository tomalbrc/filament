package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class StringNotEmptyCondition implements Condition {
    private final String var;

    StringNotEmptyCondition(String var) {
        this.var = var;
    }

    public boolean test(SkillTree ctx, Target target) {
        Object v = ctx.vars().get(var);
        return v != null && !v.toString().isEmpty();
    }
}
