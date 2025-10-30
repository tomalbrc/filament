package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;

class AltitudeCondition implements Condition {
    private final double minAboveGround;

    AltitudeCondition(double minAboveGround) {
        this.minAboveGround = minAboveGround;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        BlockPos pos = new BlockPos(Mth.floor(target.getX()), Mth.floor(target.getY()), Mth.floor(target.getZ()));
        int groundY = ctx.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
        return (target.getY() - groundY) >= minAboveGround;
    }
}
