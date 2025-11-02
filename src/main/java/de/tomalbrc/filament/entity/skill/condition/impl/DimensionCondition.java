package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DimensionCondition implements Condition {
    private final ResourceKey<Level> dim;
    DimensionCondition(ResourceKey<Level> dim){this.dim=dim;}
    public boolean test(SkillTree ctx, Target target){ return ctx.level().dimension() == dim; }
}