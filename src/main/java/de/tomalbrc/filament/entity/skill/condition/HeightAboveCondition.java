package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class HeightAboveCondition implements Condition {
    private final double y;

    HeightAboveCondition(double y) {
        this.y = y;
    }

    public boolean test(SkillContext ctx, Target target) {
        return target.getPosition().y() > y;
    }
}
