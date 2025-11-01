package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

// TODO
class BowTensionCondition implements Condition {
    private final float min, max;
    BowTensionCondition(float min, float max) { this.min=min; this.max=max; }
    public boolean test(SkillContext ctx, Target target) { return true; }
}
