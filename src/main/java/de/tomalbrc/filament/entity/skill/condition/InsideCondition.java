package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

class InsideCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        BlockPos pos = BlockPos.containing(target.getX(), target.getEyeY() + 0.5, target.getZ());
        return !ctx.level().canSeeSky(pos);
    }
}
