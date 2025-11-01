package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class NightCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        long time = ctx.level().getDayTime() % 24000;
        return time >= 14000 && time <= 22000;
    }
}
