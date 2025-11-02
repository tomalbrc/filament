package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class DistanceCondition implements Condition {
    private final double min, max;
    DistanceCondition(double min,double max){this.min=min;this.max=max;}
    public boolean test(SkillTree ctx, Target target){
        double d = ctx.caster().distanceToSqr(target.getPosition());
        return d>=min*min && d<=max*max;
    }
}
