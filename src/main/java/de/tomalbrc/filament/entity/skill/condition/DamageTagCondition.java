package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

class DamageTagCondition implements Condition {
    private final ResourceLocation tag;

    DamageTagCondition(ResourceLocation t) {
        this.tag = t;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().getOrDefault("damageTags", Variable.EMPTY).asSet().contains(tag);
    }
}
