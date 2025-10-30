package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class IsBabyCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        return target.isBaby();
    }
}
