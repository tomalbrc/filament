package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

import java.util.Objects;

public class GlidingCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        return Objects.requireNonNull(target.getEntity().asLivingEntity()).isFallFlying();
    }
}
