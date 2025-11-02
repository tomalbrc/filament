package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class NameCondition implements Condition {
    private final String name;

    NameCondition(String n) {
        this.name = n;
    }

    public boolean test(SkillTree ctx, Target target) {
        return target.getEntity().getName().getString().equals(name);
    }
}
