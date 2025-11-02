package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

public class LookingAtCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        Vec3 dir = ctx.caster().getLookAngle().normalize();
        Vec3 to = target.getPosition().subtract(ctx.caster().position()).normalize();
        return dir.dot(to) >= 0.98;
    }
}
