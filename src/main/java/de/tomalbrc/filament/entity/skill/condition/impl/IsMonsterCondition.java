package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class IsMonsterCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        return !target.getEntity().getType().getCategory().isFriendly();
    }
}
