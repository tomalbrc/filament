package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

class DistanceFromSpawnCondition implements Condition {
    private final double min,max;
    DistanceFromSpawnCondition(double min,double max){this.min=min;this.max=max;}
    public boolean test(SkillContext ctx, LivingEntity target){
        BlockPos spawn = ctx.level().getRespawnData().pos();
        double d = new Vec3(spawn.getX(), spawn.getY(), spawn.getZ()).distanceToSqr(target.position());
        return d>=min*min && d<=max*max;
    }
}