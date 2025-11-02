package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class DistanceFromSpawnCondition implements Condition {
    private final double min,max;
    DistanceFromSpawnCondition(double min,double max){this.min=min;this.max=max;}
    public boolean test(SkillTree ctx, Target target){
        BlockPos spawn = ctx.level().getRespawnData().pos();
        double d = new Vec3(spawn.getX(), spawn.getY(), spawn.getZ()).distanceToSqr(target.getPosition());
        return d>=min*min && d<=max*max;
    }
}