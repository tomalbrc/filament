package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class TargetWithinCondition implements Condition {
    private final double r;

    TargetWithinCondition(double r) {
        this.r = r;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        LivingEntity t = ctx.vars().get("triggeringEntity") instanceof LivingEntity e ? e : null;
        if (t == null) return false;
        return t.distanceToSqr(target) <= r * r;
    }
}
