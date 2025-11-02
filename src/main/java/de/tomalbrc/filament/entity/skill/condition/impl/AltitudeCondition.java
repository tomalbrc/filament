package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class AltitudeCondition implements Condition {
    private final double minAboveGround;

    AltitudeCondition(double minAboveGround) {
        this.minAboveGround = minAboveGround;
    }

    public boolean test(SkillTree ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        int groundY = ctx.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
        return (target.getPosition().y() - groundY) >= minAboveGround;
    }
}
