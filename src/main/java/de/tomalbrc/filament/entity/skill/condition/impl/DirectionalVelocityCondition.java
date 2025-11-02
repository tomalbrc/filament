package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.world.phys.Vec3;

public class DirectionalVelocityCondition implements Condition {
    private final Vec3 min, max;
    DirectionalVelocityCondition(Vec3 min, Vec3 max){this.min=min;this.max=max;}
    public boolean test(SkillTree ctx, Target target){
        Vec3 v = target.getEntity().getDeltaMovement();
        return v.x>=min.x&&v.y>=min.y&&v.z>=min.z&&v.x<=max.x&&v.y<=max.y&&v.z<=max.z;
    }
}