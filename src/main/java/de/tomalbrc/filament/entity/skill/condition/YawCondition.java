package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class YawCondition implements Condition {
    private final double min, max;

    YawCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double yaw = target.getYRot();
        return yaw >= min && yaw <= max;
    }
}
