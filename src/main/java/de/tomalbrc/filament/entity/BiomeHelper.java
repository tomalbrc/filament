package de.tomalbrc.filament.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Predicate;

public class BiomeHelper {
    public static void addSpawn(EntityType<?> type, int weight, int minGroupSize, int maxGroupSize, Predicate<BiomeSelectionContext> selector) {
        BiomeHelper.addSpawn(type, type.getCategory(), weight, minGroupSize, maxGroupSize, selector);
    }

    public static void addSpawn(EntityType<?> type, MobCategory category, int weight, int minGroupSize, int maxGroupSize, Predicate<BiomeSelectionContext> selector) {
        BiomeModifications.addSpawn(selector, category, type, weight, minGroupSize, maxGroupSize);
    }

    public static Predicate<BiomeSelectionContext> excludeTag(TagKey<Biome> tag) {
        return context -> !context.hasTag(tag);
    }

    public static Predicate<BiomeSelectionContext> includeTag(TagKey<Biome> tag) {
        return context -> context.hasTag(tag);
    }
}
