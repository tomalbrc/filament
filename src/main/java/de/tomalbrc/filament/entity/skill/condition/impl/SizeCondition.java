package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class SizeCondition implements Condition {
    private final float min, max;

    SizeCondition(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        float s = target.getEntity().getBbWidth();
        return s >= min && s <= max;
    }
}
