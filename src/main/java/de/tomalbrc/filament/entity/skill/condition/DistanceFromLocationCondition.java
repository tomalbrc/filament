package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

class DistanceFromLocationCondition implements Condition {
    private final Vec3 loc; private final double min,max;
    DistanceFromLocationCondition(Vec3 loc,double min,double max){this.loc=loc;this.min=min;this.max=max;}
    public boolean test(SkillContext ctx, LivingEntity target){
        double d = loc.distanceToSqr(target.position());
        return d>=min*min && d<=max*max;
    }
}