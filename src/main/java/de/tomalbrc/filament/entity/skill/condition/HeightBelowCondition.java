package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class HeightBelowCondition implements Condition {
    private final double y;

    HeightBelowCondition(double y) {
        this.y = y;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return target.getY() < y;
    }
}
