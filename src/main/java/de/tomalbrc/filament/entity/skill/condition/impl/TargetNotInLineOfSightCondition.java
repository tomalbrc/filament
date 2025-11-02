package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.entity.LivingEntity;

public class TargetNotInLineOfSightCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        LivingEntity t = ctx.caster().getTarget();
        if (t == null) return true;
        return !t.hasLineOfSight(ctx.caster());
    }
}
