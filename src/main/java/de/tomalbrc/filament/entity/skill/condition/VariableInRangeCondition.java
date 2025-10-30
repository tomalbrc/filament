package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class VariableInRangeCondition implements Condition {
    private final String var;
    private final double min, max;

    VariableInRangeCondition(String var, double min, double max) {
        this.var = var;
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object v = ctx.vars().get(var);
        if (!(v instanceof Number n)) return false;
        double val = n.doubleValue();
        return val >= min && val <= max;
    }
}
