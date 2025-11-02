package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class LightLevelCondition implements Condition {
    private final int min, max;

    LightLevelCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillTree ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        int l = ctx.level().getBrightness(LightLayer.BLOCK, pos);
        return l >= min && l <= max;
    }
}
