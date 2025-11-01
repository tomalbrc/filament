package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

class AltitudeCondition implements Condition {
    private final double minAboveGround;

    AltitudeCondition(double minAboveGround) {
        this.minAboveGround = minAboveGround;
    }

    public boolean test(SkillContext ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        int groundY = ctx.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
        return (target.getPosition().y() - groundY) >= minAboveGround;
    }
}
