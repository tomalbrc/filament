package de.tomalbrc.filament.data.entity;

import com.google.common.collect.ImmutableSet;
import de.tomalbrc.filament.entity.BiomeHelper;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biomes;

import java.util.Set;
import java.util.function.Predicate;

public record SpawnInfo(
        int weight,
        int minGroupSize,
        int maxGroupSize,
        boolean foundInOverworld,
        boolean foundInNether,
        boolean foundInEnd,
        Set<ResourceLocation> spawnsLike,
        Set<ResourceLocation> biomes,
        Set<ResourceLocation> biomeTags
) {
    public void add(EntityType<?> entityType) {
        Set<EntityType<?>> spawns = spawnsLike == null ? ImmutableSet.of() : null;
        if (spawns == null) {
            spawns = new ObjectArraySet<>();
            for (ResourceLocation resourceLocation : spawnsLike) {
                spawns.add(BuiltInRegistries.ENTITY_TYPE.getValue(resourceLocation));
            }
        }

        Predicate<BiomeSelectionContext> biomeSelectors = spawns.isEmpty() ? ((x) -> false) : BiomeSelectors.spawnsOneOf(spawns);
        if (biomes != null)
            biomeSelectors = biomeSelectors.or(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP));
        if (biomeTags != null) {
            for (ResourceLocation tag : biomeTags) {
                biomeSelectors = biomeSelectors.or(BiomeSelectors.tag(TagKey.create(Registries.BIOME, tag)));
            }
        }

        if (foundInOverworld) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInOverworld());
        if (foundInNether) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInTheNether());
        if (foundInEnd) biomeSelectors = biomeSelectors.and(BiomeSelectors.foundInTheEnd());

        BiomeHelper.addSpawn(entityType, weight, minGroupSize, maxGroupSize, biomeSelectors);
    }
}
