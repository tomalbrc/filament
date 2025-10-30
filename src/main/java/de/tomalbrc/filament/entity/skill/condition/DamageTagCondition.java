package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class DamageTagCondition implements Condition {
    private final String tag;

    DamageTagCondition(String t) {
        this.tag = t;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().getOrDefault("damageTags", "").toString().contains(tag);
    }
}
