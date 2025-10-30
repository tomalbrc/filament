package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

class BoundingBoxesOverlapCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        AABB a = ctx.caster().getBoundingBox();
        AABB b = target.getBoundingBox();
        return a.intersects(b);
    }
}