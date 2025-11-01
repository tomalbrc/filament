package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class SkillOnCooldownCondition implements Condition {
    private final String skill;

    SkillOnCooldownCondition(String s) {
        this.skill = s;
    }

    public boolean test(SkillContext ctx, Target target) {
        Object cd = ctx.vars().get("cooldown:" + skill);
        return cd instanceof Integer i && i > 0;
    }
}