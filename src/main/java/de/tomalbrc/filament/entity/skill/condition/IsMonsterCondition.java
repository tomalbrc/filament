package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class IsMonsterCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return !target.getEntity().getType().getCategory().isFriendly();
    }
}
