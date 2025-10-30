package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class DistanceCondition implements Condition {
    private final double min, max;
    DistanceCondition(double min,double max){this.min=min;this.max=max;}
    public boolean test(SkillContext ctx, LivingEntity target){
        double d = ctx.caster().distanceToSqr(target);
        return d>=min*min && d<=max*max;
    }
}