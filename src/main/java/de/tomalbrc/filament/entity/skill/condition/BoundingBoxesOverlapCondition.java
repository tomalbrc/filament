package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.AABB;

class BoundingBoxesOverlapCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        AABB a = ctx.caster().getBoundingBox();
        AABB b = target.getEntity().getBoundingBox();
        return a.intersects(b);
    }
}