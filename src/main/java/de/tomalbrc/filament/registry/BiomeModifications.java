package de.tomalbrc.filament.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class BiomeModifications {
    public static final Map<Identifier, JsonObject> TO_REGISTER = new Object2ObjectOpenHashMap<>();
    private static boolean locked = false;

    public record AddFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                           GenerationStep.Decoration step) {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "add_features");
        public static final MapCodec<AddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(
                                Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
                                PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
                                GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step))
                        .apply(builder, AddFeaturesBiomeModifier::new)
        );
    }

    public static void register(InputStream inputStream, Identifier id) throws IOException {
        var json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        TO_REGISTER.put(id, json);
    }

    public static void addAll(LayeredRegistryAccess<RegistryLayer> server) {
        for (Map.Entry<Identifier, JsonObject> element : TO_REGISTER.entrySet()) {
            var type = element.getValue().get("type").getAsJsonPrimitive().getAsString();
            if (Identifier.parse(type).equals(AddFeaturesBiomeModifier.ID)) {
                var dataResult = AddFeaturesBiomeModifier.CODEC.decoder().decode(RegistryOps.create(JsonOps.INSTANCE, server.compositeAccess()), element.getValue());
                var modifier = dataResult.getOrThrow().getFirst();
                addFeatureModifier(modifier);
            }
        }
        TO_REGISTER.clear();
        locked = true;
    }

    private static void addFeatureModifier(AddFeaturesBiomeModifier modifier) {
        for (Holder<PlacedFeature> featureHolder : modifier.features) {
            featureHolder.unwrapKey().ifPresent(feature -> {
                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(x -> {
                    for (Holder<Biome> biomeHolder : modifier.biomes) {
                        if (x.getBiomeRegistryEntry().is(biomeHolder))
                            return true;
                    }

                    return false;
                }, modifier.step, feature);
            });
        }
    }

    public static class BiomeModificationsDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "biome_modifications");
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
