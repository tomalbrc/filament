package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class BiomeTypeCondition implements Condition {
    private final Biome.Precipitation type;

    BiomeTypeCondition(Biome.Precipitation type) {
        this.type = type;
    }

    public boolean test(SkillTree ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        Biome biome = ctx.level().getBiome(pos).value();
        return biome.getPrecipitationAt(pos, ctx.level().getSeaLevel()) == type;
    }
}