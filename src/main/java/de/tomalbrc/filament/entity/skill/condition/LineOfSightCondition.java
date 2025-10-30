package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

class LineOfSightCondition implements Condition {
    private final double range;

    LineOfSightCondition(double r) {
        this.range = r;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        return Objects.requireNonNull(ctx.caster().asLivingEntity()).hasLineOfSight(target) && target.distanceTo(ctx.caster()) <= range;
    }
}
