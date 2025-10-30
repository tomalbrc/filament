package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class ThunderingCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.level().isThundering();
    }
}
