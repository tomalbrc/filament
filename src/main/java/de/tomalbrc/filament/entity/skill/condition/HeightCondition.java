package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class HeightCondition implements Condition {
    private final double min, max;

    HeightCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double y = target.getY();
        return y >= min && y <= max;
    }
}
