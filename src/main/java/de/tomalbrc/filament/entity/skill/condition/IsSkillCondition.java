package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class IsSkillCondition implements Condition {
    private final String skillName;

    IsSkillCondition(String s) {
        this.skillName = s;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.vars().containsKey("skills:" + skillName);
    }
}