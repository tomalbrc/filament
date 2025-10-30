package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class VariableIsSetCondition implements Condition {
    private final String var;

    VariableIsSetCondition(String var) {
        this.var = var;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().containsKey(var);
    }
}
