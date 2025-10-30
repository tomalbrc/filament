package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class FallSpeedCondition implements Condition {
    private final double min, max;

    FallSpeedCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double v = Math.abs(target.getDeltaMovement().y);
        return v >= min && v <= max;
    }
}