package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;

public class OnBlockCondition implements Condition {
    public boolean test(SkillTree ctx, Target target) {
        BlockPos pos = target.getEntity().getOnPos();
        return !ctx.level().getBlockState(pos).isAir();
    }
}
