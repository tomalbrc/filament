package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class HasPassengerCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return !target.getEntity().getPassengers().isEmpty();
    }
}
