package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class StringEmptyCondition implements Condition {
    private final String var;

    StringEmptyCondition(String var) {
        this.var = var;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object v = ctx.vars().get(var);
        return v == null || v.toString().isEmpty();
    }
}