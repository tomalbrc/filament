package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class VariableContainsCondition implements Condition {
    private final String var;
    private final String value;

    VariableContainsCondition(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object v = ctx.vars().get(var);
        return v != null && v.toString().contains(value);
    }
}
