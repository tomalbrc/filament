package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.target.Target;

public abstract class AbstractCondition implements Condition {

    @Override
    public boolean test(SkillTree context, Target target) {
        return false;
    }
}
