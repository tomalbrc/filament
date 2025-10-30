package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class LastDamageCauseCondition implements Condition {
    private final String cause;

    LastDamageCauseCondition(String c) {
        this.cause = c;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object o = ctx.vars().get("lastDamageCause");
        return o != null && o.toString().equalsIgnoreCase(cause);
    }
}