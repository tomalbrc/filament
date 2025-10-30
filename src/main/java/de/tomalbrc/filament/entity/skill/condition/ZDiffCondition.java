package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class ZDiffCondition implements Condition {
    private final double min, max;

    ZDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double d = Math.abs(target.getZ() - ctx.caster().getZ());
        return d >= min && d <= max;
    }
}
