package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

class WorldCondition implements Condition {
    private final String worldName;

    WorldCondition(String w) {
        this.worldName = w;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.level().dimension().location().toString().equals(worldName);
    }
}
