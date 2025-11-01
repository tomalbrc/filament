package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class DawnCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        long time = ctx.level().getDayTime() % 24000;
        return time >= 22000 || time <= 2000;
    }
}
