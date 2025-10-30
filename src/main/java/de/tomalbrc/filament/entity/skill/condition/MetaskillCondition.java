package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.Variable;
import net.minecraft.world.entity.LivingEntity;

class MetaskillCondition implements Condition {
    private final String meta;

    MetaskillCondition(String m) {
        this.meta = m;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return ctx.vars().getOrDefault("metaLastResult:" + meta, Variable.EMPTY).asBoolean() == Boolean.TRUE;
    }
}