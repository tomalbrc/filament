package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class SprintingCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return target.getEntity().isSprinting();
    }
}
