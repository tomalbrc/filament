package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class HasTagCondition implements Condition {
    private final String tag;

    HasTagCondition(String t) {
        this.tag = t;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return target.getTags().contains(tag);
    }
}
