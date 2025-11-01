package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

class LightLevelCondition implements Condition {
    private final int min, max;

    LightLevelCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        int l = ctx.level().getBrightness(LightLayer.BLOCK, pos);
        return l >= min && l <= max;
    }
}
