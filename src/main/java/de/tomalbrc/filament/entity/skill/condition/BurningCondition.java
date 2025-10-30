package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class BurningCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) { return target.isOnFire(); }
}