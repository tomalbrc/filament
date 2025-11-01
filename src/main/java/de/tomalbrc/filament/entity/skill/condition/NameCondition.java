package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class NameCondition implements Condition {
    private final String name;

    NameCondition(String n) {
        this.name = n;
    }

    public boolean test(SkillContext ctx, Target target) {
        return target.getEntity().getName().getString().equals(name);
    }
}
