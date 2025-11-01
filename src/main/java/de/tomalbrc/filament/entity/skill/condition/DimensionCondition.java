package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

class DimensionCondition implements Condition {
    private final ResourceKey<Level> dim;
    DimensionCondition(ResourceKey<Level> dim){this.dim=dim;}
    public boolean test(SkillContext ctx, Target target){ return ctx.level().dimension() == dim; }
}