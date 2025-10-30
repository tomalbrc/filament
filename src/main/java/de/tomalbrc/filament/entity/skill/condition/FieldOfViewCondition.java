package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

class FieldOfViewCondition implements Condition {
    private final double angle;

    FieldOfViewCondition(double angle) {
        this.angle = angle;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Vec3 look = ctx.caster().getLookAngle().normalize();
        Vec3 to = target.position().subtract(ctx.caster().position()).normalize();
        double dot = look.dot(to);
        double deg = Math.acos(dot) * 180 / Math.PI;
        return deg <= angle;
    }
}