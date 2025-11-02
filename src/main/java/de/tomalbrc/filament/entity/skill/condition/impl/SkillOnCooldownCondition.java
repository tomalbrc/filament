package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class SkillOnCooldownCondition implements Condition {
    private final String skill;

    SkillOnCooldownCondition(String s) {
        this.skill = s;
    }

    public boolean test(SkillTree ctx, Target target) {
        Object cd = ctx.vars().get("cooldown:" + skill);
        return cd instanceof Integer i && i > 0;
    }
}