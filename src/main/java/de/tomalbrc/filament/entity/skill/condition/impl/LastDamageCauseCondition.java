package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class LastDamageCauseCondition implements Condition {
    private final String cause;

    LastDamageCauseCondition(String c) {
        this.cause = c;
    }

    public boolean test(SkillTree ctx, Target target) {
        Object o = ctx.vars().get("lastDamageCause");
        return o != null && o.toString().equalsIgnoreCase(cause);
    }
}