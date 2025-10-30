package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class DuskCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        long time = ctx.level().getDayTime() % 24000;
        return time >= 14000 && time <= 18000;
    }
}
