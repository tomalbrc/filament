package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;

class LightLevelCondition implements Condition {
    private final int min, max;

    LightLevelCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        BlockPos pos = target.blockPosition();
        int l = ctx.level().getBrightness(LightLayer.BLOCK, pos);
        return l >= min && l <= max;
    }
}
