package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.Variable;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;

public class MetaskillCondition implements Condition {
    private final String meta;

    MetaskillCondition(String m) {
        this.meta = m;
    }

    public boolean test(SkillTree ctx, Target target) {
        return ctx.vars().getOrDefault("metaLastResult:" + meta, Variable.EMPTY).asBoolean() == Boolean.TRUE;
    }
}