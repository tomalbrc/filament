package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class TargetNotInLineOfSightCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        LivingEntity t = ctx.caster().getTarget();
        if (t == null) return true;
        return !t.hasLineOfSight(ctx.caster());
    }
}
