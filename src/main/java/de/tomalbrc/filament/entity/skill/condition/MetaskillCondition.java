package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.target.Target;

class MetaskillCondition implements Condition {
    private final String meta;

    MetaskillCondition(String m) {
        this.meta = m;
    }

    public boolean test(SkillContext ctx, Target target) {
        return ctx.vars().getOrDefault("metaLastResult:" + meta, Variable.EMPTY).asBoolean() == Boolean.TRUE;
    }
}