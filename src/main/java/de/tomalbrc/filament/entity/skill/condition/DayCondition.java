package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class DayCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        long time = ctx.level().getDayTime() % 24000;
        return time >= 2000 && time <= 10000;
    }
}
