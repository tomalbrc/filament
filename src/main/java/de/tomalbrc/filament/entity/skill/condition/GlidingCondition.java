package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

class GlidingCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        return Objects.requireNonNull(target.getEntity().asLivingEntity()).isFallFlying();
    }
}
