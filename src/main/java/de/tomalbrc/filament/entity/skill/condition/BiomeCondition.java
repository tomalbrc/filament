package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

class BiomeCondition implements Condition {
    private final Set<ResourceLocation> biomes;

    BiomeCondition(Set<ResourceLocation> ids) {
        this.biomes = ids;
    }

    public boolean test(SkillContext ctx, LivingEntity target) {
        BlockPos pos = target.blockPosition();
        var key = ctx.level().getBiome(pos).unwrapKey();
        ResourceLocation id = null;
        if (key.isPresent()) {
            id = key.orElseThrow().location();
        }
        return id != null && biomes.contains(id);
    }
}