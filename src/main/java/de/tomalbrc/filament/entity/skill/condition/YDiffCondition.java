package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class YDiffCondition implements Condition {
    private final double min, max;

    YDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double d = Math.abs(target.getY() - ctx.caster().getY());
        return d >= min && d <= max;
    }
}
