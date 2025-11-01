package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class SizeCondition implements Condition {
    private final float min, max;

    SizeCondition(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        float s = target.getEntity().getBbWidth();
        return s >= min && s <= max;
    }
}
