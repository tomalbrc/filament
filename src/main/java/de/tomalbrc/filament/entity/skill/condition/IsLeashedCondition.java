package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;

class IsLeashedCondition implements Condition {
    public boolean test(SkillContext ctx, LivingEntity target) {
        return target instanceof Leashable leashable && leashable.isLeashed();
    }
}
