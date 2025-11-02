package de.tomalbrc.filament.entity.skill.condition.impl;

import de.tomalbrc.filament.entity.skill.SkillTree;
import de.tomalbrc.filament.entity.skill.condition.Condition;
import de.tomalbrc.filament.entity.skill.target.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class BiomeCondition implements Condition {
    private final Set<ResourceLocation> biomes;

    BiomeCondition(Set<ResourceLocation> ids) {
        this.biomes = ids;
    }

    public boolean test(SkillTree ctx, Target target) {
        BlockPos pos = target.getBlockPos();
        var key = ctx.level().getBiome(pos).unwrapKey();
        ResourceLocation id = null;
        if (key.isPresent()) {
            id = key.orElseThrow().location();
        }
        return id != null && biomes.contains(id);
    }
}