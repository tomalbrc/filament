package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

class DistanceFromLocationCondition implements Condition {
    private final Vec3 loc; private final double min,max;
    DistanceFromLocationCondition(Vec3 loc,double min,double max){this.loc=loc;this.min=min;this.max=max;}
    public boolean test(SkillContext ctx, Target target){
        double d = loc.distanceToSqr(target.getPosition());
        return d>=min*min && d<=max*max;
    }
}