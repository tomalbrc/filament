package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class MovingCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return target.getEntity().getDeltaMovement().lengthSqr() > 0.001;
    }
}
