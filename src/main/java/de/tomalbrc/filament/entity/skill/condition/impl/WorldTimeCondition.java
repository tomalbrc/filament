package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class WorldTimeCondition implements Condition {
    private final long min, max;

    WorldTimeCondition(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        long t = ctx.level().getDayTime() % 24000;
        return t >= min && t <= max;
    }
}
