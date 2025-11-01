package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;

class HasTagCondition implements Condition {
    private final String tag;

    HasTagCondition(String t) {
        this.tag = t;
    }

    public boolean test(SkillContext ctx, Target target) {
        return target.getEntity().getTags().contains(tag);
    }
}
