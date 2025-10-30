package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class ChanceCondition implements Condition {
    private final double chance;

    ChanceCondition(double chance) {
        this.chance = chance;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.caster().getRandom().nextDouble() <= chance;
    }
}