package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

class TargetNotWithinCondition implements Condition {
    private final double r;

    TargetNotWithinCondition(double r) {
        this.r = r;
    }

    public boolean test(SkillContext ctx, Target target) {
        LivingEntity t = ctx.caster().getTarget();
        if (t == null) return true;
        return t.distanceToSqr(target.getPosition()) > r * r;
    }
}
