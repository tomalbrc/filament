package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class SizeCondition implements Condition {
    private final float min, max;

    SizeCondition(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        float s = target.getBbWidth();
        return s >= min && s <= max;
    }
}
