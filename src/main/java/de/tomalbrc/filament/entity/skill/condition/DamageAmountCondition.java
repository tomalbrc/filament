package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import net.minecraft.world.entity.LivingEntity;

class DamageAmountCondition implements Condition {
    private final double min, max;

    DamageAmountCondition(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        var n = ctx.vars().getOrDefault("lastDamage", Variable.EMPTY).asNumber();
        return n != null && n.doubleValue() >= min && n.doubleValue() <= max;
    }
}