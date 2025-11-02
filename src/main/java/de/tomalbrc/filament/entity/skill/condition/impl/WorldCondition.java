package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class WorldCondition implements Condition {
    private final String worldName;

    WorldCondition(String w) {
        this.worldName = w;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.level().dimension().location().toString().equals(worldName);
    }
}
