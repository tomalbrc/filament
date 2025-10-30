package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class IsSkillCondition implements Condition {
    private final String skillName;

    IsSkillCondition(String s) {
        this.skillName = s;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().containsKey("skills:" + skillName);
    }
}