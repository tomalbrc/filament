package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class HeightAboveCondition implements Condition {
    private final double y;

    HeightAboveCondition(double y) {
        this.y = y;
    }

    public boolean test(SkillTree ctx, Target target) {
        return target.getPosition().y() > y;
    }
}
