package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

class TargetNotInLineOfSightCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        LivingEntity t = ctx.caster().getTarget();
        if (t == null) return true;
        return !t.hasLineOfSight(ctx.caster());
    }
}
