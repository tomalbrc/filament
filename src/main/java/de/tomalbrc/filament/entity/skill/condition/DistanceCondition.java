package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class DistanceCondition implements Condition {
    private final double min, max;
    DistanceCondition(double min,double max){this.min=min;this.max=max;}
    public boolean test(SkillContext ctx, Target target){
        double d = ctx.caster().distanceToSqr(target.getPosition());
        return d>=min*min && d<=max*max;
    }
}