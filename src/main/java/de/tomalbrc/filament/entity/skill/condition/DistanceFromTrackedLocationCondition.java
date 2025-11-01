package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

class DistanceFromTrackedLocationCondition implements Condition {
    private final String var;
    private final double min, max;

    DistanceFromTrackedLocationCondition(String var, double min, double max) {
        this.var = var;
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        Object o = ctx.vars().get(var);
        if (!(o instanceof Vec3 v)) return false;
        double d = v.distanceToSqr(target.getPosition());
        return d >= min * min && d <= max * max;
    }
}