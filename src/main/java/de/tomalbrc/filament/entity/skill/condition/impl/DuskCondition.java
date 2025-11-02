package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class DuskCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        long time = ctx.level().getDayTime() % 24000;
        return time >= 14000 && time <= 18000;
    }
}
