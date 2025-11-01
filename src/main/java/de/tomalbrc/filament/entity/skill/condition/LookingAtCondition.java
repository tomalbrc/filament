package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

class LookingAtCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        Vec3 dir = ctx.caster().getLookAngle().normalize();
        Vec3 to = target.getPosition().subtract(ctx.caster().position()).normalize();
        return dir.dot(to) >= 0.98;
    }
}
