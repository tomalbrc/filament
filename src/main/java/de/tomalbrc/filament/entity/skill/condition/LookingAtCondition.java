package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

class LookingAtCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        Vec3 dir = ctx.caster().getLookAngle().normalize();
        Vec3 to = target.position().subtract(ctx.caster().position()).normalize();
        return dir.dot(to) >= 0.98;
    }
}
