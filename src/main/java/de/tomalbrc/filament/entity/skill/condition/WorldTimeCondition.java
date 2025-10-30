package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class WorldTimeCondition implements Condition {
    private final long min, max;

    WorldTimeCondition(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        long t = ctx.level().getDayTime() % 24000;
        return t >= min && t <= max;
    }
}
