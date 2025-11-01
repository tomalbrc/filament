package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

class TargetInLineOfSightCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        LivingEntity t = ctx.caster().getTarget();
        if (t == null) return false;
        return t.hasLineOfSight(ctx.caster());
    }
}
