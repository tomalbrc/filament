package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class XDiffCondition implements Condition {
    private final double min, max;

    XDiffCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        double d = Math.abs(target.getX() - ctx.caster().getX());
        return d >= min && d <= max;
    }
}
