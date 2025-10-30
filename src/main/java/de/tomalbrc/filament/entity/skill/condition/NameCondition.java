package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class NameCondition implements Condition {
    private final String name;

    NameCondition(String n) {
        this.name = n;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return target.getName().getString().equals(name);
    }
}
