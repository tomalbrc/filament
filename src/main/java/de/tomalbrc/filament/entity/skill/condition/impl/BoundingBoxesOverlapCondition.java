package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.AABB;

public class BoundingBoxesOverlapCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        AABB a = ctx.caster().getBoundingBox();
        AABB b = target.getEntity().getBoundingBox();
        return a.intersects(b);
    }
}