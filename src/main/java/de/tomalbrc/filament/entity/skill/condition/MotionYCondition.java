package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class MotionYCondition implements Condition {
    private final double min, max;

    MotionYCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double v = target.getDeltaMovement().y;
        return v >= min && v <= max;
    }
}
