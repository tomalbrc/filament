package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class DamageAmountCondition implements Condition {
    private final double min, max;

    DamageAmountCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().getOrDefault("lastDamage", 0) instanceof Number n && n.doubleValue() >= min && n.doubleValue() <= max;
    }
}