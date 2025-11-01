package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

class FieldOfViewCondition implements Condition {
    private final double angle;

    FieldOfViewCondition(double angle) {
        this.angle = angle;
    }

    public boolean test(SkillContext ctx, Target target) {
        Vec3 look = ctx.caster().getLookAngle().normalize();
        Vec3 to = target.getPosition().subtract(ctx.caster().position()).normalize();
        double dot = look.dot(to);
        double deg = Math.acos(dot) * 180 / Math.PI;
        return deg <= angle;
    }
}