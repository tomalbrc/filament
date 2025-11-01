package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class WorldCondition implements Condition {
    private final String worldName;

    WorldCondition(String w) {
        this.worldName = w;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.level().dimension().location().toString().equals(worldName);
    }
}
