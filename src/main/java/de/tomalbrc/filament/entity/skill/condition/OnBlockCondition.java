package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;

class OnBlockCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        BlockPos pos = target.getEntity().getOnPos();
        return !ctx.level().getBlockState(pos).isAir();
    }
}
