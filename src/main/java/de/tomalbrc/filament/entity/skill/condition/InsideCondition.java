package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;

class InsideCondition implements Condition {
    public boolean test(SkillContext ctx, Target target) {
        BlockPos pos = BlockPos.containing(target.getEntity().getX(), target.getEntity().getEyeY() + 0.5, target.getEntity().getZ());
        return !ctx.level().canSeeSky(pos);
    }
}
