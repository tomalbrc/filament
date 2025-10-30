package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class SkillOnCooldownCondition implements Condition {
    private final String skill;

    SkillOnCooldownCondition(String s) {
        this.skill = s;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        Object cd = ctx.vars().get("cooldown:" + skill);
        return cd instanceof Integer i && i > 0;
    }
}