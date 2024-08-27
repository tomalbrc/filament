package de.tomalbrc.filament;

import de.tomalbrc.filament.behaviours.item.Armor;
import de.tomalbrc.filament.util.Json;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.lwjgl.util.freetype.FreeType;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FilamentTrimPatterns {
    public static class FilamentTrimHolder {
        public TrimPattern trimPattern;
    }
    private static Map<Armor.ArmorConfig, FilamentTrimHolder> trimConfigs = new Object2ObjectOpenHashMap();

    public static final ResourceKey<TrimPattern> FILAMENT_TEST = of("filament-test");

    public static FilamentTrimHolder addConfig(Armor.ArmorConfig config) {
        var tmpHolder = trimConfigs.get(config);
        if (tmpHolder != null)
            return tmpHolder;

        FilamentTrimHolder holder = new FilamentTrimHolder();
        trimConfigs.put(config, holder);
        return holder;
    }

    public static void bootstrap(BootstrapContext<TrimPattern> registry) {
        register(registry, Items.BARRIER, FILAMENT_TEST);

        // TODO: add to resourcepack contents ..?
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(FilamentTrimPatterns::addRPContents);
    }

    public static void addRPContents(ResourcePackBuilder builder) {
        String path = "assets/minecraft/atlases/armor_trims.json";

        var data = builder.getDataOrSource(path);
        ResourcePackTrimPatternAtlas atlas = Json.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), ResourcePackTrimPatternAtlas.class);

        ResourcePackTrimPatternAtlas.Source source = new ResourcePackTrimPatternAtlas.Source();
        source.type = atlas.sources.get(0).type;
        source.palette_key = atlas.sources.get(0).palette_key;

        for (Map.Entry<Armor.ArmorConfig, FilamentTrimHolder> entry : trimConfigs.entrySet()) {
            String texturePath = entry.getKey().texture.getNamespace() + ":models/armor/" + entry.getKey().texture.getPath();
            source.textures.add(texturePath);
        }

        source.permutations = atlas.sources.get(0).permutations;

        atlas.sources.add(source);

        builder.addData(path, Json.GSON.toJson(atlas).getBytes(StandardCharsets.UTF_8));
    }

    private static void register(BootstrapContext<TrimPattern> registry, Item template, ResourceKey<TrimPattern> key) {
        TrimPattern armorTrimPattern = new TrimPattern(
                key.location(),
                BuiltInRegistries.ITEM.wrapAsHolder(template),
                Component.literal("filament.dummy"),
                false // decal
        );
        registry.register(key, armorTrimPattern);
    }
    private static ResourceKey<TrimPattern> of(String id) {
        return ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.fromNamespaceAndPath("filament", id));
    }

    private FilamentTrimPatterns() {

    }
}