package de.tomalbrc.filament.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BiomeModifications {
    public static final Map<ResourceLocation, JsonObject> TO_REGISTER = new Object2ObjectOpenHashMap<>();
    private static boolean locked = false;

    public record AddFeaturesData(HolderSet<PlacedFeature> features, GenerationStep.Decoration step) {
        public static final MapCodec<AddFeaturesData> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(
                                PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesData::features),
                                GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesData::step))
                        .apply(builder, AddFeaturesData::new));
    }

    public static final ResourceLocation ADD_FEATURES_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "add_features");

    public static void register(InputStream inputStream, ResourceLocation id) throws IOException {
        var json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        validateBiomesField(json, id);
        TO_REGISTER.put(id, json);
    }

    private static void validateBiomesField(JsonObject json, ResourceLocation fileId) {
        JsonElement biomesElement = json.get("biomes");
        if (biomesElement == null) {
            Filament.LOGGER.warn("Biome modification \"{}\" has no 'biomes' field, will match all biomes", fileId);
            return;
        }

        if (biomesElement.isJsonPrimitive()) {
            validateSingleBiomeEntry(biomesElement.getAsString(), fileId);
        } else if (biomesElement.isJsonArray()) {
            JsonArray array = biomesElement.getAsJsonArray();
            if (array.isEmpty()) {
                Filament.LOGGER.warn("Biome modification \"{}\" has empty 'biomes' array, will match all biomes",
                        fileId);
            }
            for (JsonElement elem : array) {
                if (elem.isJsonPrimitive()) {
                    validateSingleBiomeEntry(elem.getAsString(), fileId);
                } else {
                    Filament.LOGGER.error("Biome modification \"{}\" has invalid biome entry (not a string): {}",
                            fileId, elem);
                }
            }
        } else {
            Filament.LOGGER.error(
                    "Biome modification \"{}\" has invalid 'biomes' field type (expected string or array): {}", fileId,
                    biomesElement);
        }
    }

    private static void validateSingleBiomeEntry(String value, ResourceLocation fileId) {
        if (value == null || value.isBlank()) {
            Filament.LOGGER.error("Biome modification \"{}\" has empty biome entry", fileId);
            return;
        }

        String toValidate = value.startsWith("#") ? value.substring(1) : value;

        if (ResourceLocation.tryParse(toValidate) == null) {
            Filament.LOGGER.error("Biome modification \"{}\" has invalid biome/tag identifier: \"{}\"", fileId, value);
        } else {
            Filament.LOGGER.info("Biome modification \"{}\" registered biome selector: {}", fileId, value);
        }
    }

    public static void addAll(LayeredRegistryAccess<RegistryLayer> server) {
        for (Map.Entry<ResourceLocation, JsonObject> element : TO_REGISTER.entrySet()) {
            var type = element.getValue().get("type").getAsJsonPrimitive().getAsString();
            if (ResourceLocation.parse(type).equals(ADD_FEATURES_ID)) {
                try {
                    Predicate<BiomeSelectionContext> biomePredicate = parseBiomePredicate(element.getValue());

                    var dataResult = AddFeaturesData.CODEC.decoder().decode(
                            RegistryOps.create(JsonOps.INSTANCE, server.compositeAccess()),
                            element.getValue());
                    var data = dataResult.getOrThrow().getFirst();

                    addFeatureModifier(biomePredicate, data);
                } catch (Exception e) {
                    Filament.LOGGER.error("Failed to parse biome modification \"{}\": {}", element.getKey(),
                            e.getMessage());
                }
            }
        }
        TO_REGISTER.clear();
        locked = true;
    }

    private static Predicate<BiomeSelectionContext> parseBiomePredicate(JsonObject json) {
        JsonElement biomesElement = json.get("biomes");

        if (biomesElement == null) {
            return ctx -> true;
        }

        List<Predicate<BiomeSelectionContext>> predicates = new ArrayList<>();

        if (biomesElement.isJsonPrimitive()) {
            var predicate = parseSingleBiomeOrTag(biomesElement.getAsString());
            if (predicate != null)
                predicates.add(predicate);
        } else if (biomesElement.isJsonArray()) {
            JsonArray array = biomesElement.getAsJsonArray();
            for (JsonElement elem : array) {
                if (elem.isJsonPrimitive()) {
                    var predicate = parseSingleBiomeOrTag(elem.getAsString());
                    if (predicate != null)
                        predicates.add(predicate);
                }
            }
        }

        if (predicates.isEmpty()) {
            return ctx -> true;
        }

        return ctx -> {
            for (Predicate<BiomeSelectionContext> predicate : predicates) {
                if (predicate.test(ctx)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Predicate<BiomeSelectionContext> parseSingleBiomeOrTag(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            if (value.startsWith("#")) {
                String tagPath = value.substring(1);
                ResourceLocation tagId = ResourceLocation.parse(tagPath);
                TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, tagId);
                return BiomeSelectors.tag(tagKey);
            } else {
                ResourceLocation biomeId = ResourceLocation.parse(value);
                return ctx -> ctx.getBiomeKey().location().equals(biomeId);
            }
        } catch (Exception e) {
            Filament.LOGGER.error("Failed to parse biome selector \"{}\": {}", value, e.getMessage());
            return null;
        }
    }

    private static void addFeatureModifier(Predicate<BiomeSelectionContext> biomePredicate, AddFeaturesData data) {
        for (Holder<PlacedFeature> featureHolder : data.features) {
            featureHolder.unwrapKey().ifPresent(feature -> {
                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                        biomePredicate,
                        data.step,
                        feature);
            });
        }
    }

    public static class BiomeModificationsDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "biome_modifications");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            if (locked) {
                return;
            }
            load("worldgen/biome_modifications", null, resourceManager, (id, inputStream) -> {
                try {
                    BiomeModifications.register(inputStream, id);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load biome modifications \"{}\".", id);
                }
            });
        }
    }
}
