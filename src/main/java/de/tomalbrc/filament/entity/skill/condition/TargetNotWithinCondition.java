package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class TargetNotWithinCondition implements Condition {
    private final double r;

    TargetNotWithinCondition(double r) {
        this.r = r;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        LivingEntity t = ctx.vars().get("triggeringEntity") instanceof LivingEntity e ? e : null;
        if (t == null) return true;
        return t.distanceToSqr(target) > r * r;
    }
}
