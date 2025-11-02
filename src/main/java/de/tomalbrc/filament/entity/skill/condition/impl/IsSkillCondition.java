package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class IsSkillCondition implements Condition {
    private final String skillName;

    IsSkillCondition(String s) {
        this.skillName = s;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.vars().containsKey("skills:" + skillName);
    }
}