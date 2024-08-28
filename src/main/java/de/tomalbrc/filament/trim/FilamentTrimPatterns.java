package de.tomalbrc.filament.trim;

import de.tomalbrc.filament.behaviours.item.Armor;
import de.tomalbrc.filament.util.FilamentConfig;
import de.tomalbrc.filament.util.Json;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FilamentTrimPatterns {
    private static final Map<Armor.ArmorConfig, FilamentTrimHolder> trimConfigs = new Object2ObjectOpenHashMap();

    private static final Map<Item, BiConsumer<RegistryAccess, ItemStack>> modelList = new Reference2ObjectOpenHashMap<>();

    public static final FilamentTrimHolder CHAIN_TRIM = addConfig(fakeChainmailConfig());

    private static Armor.ArmorConfig fakeChainmailConfig() {
        var conf = new Armor.ArmorConfig();
        conf.texture = ResourceLocation.withDefaultNamespace("chainmail");
        return conf;
    }

    public static FilamentTrimHolder addConfig(Armor.ArmorConfig config) {
        var tmpHolder = trimConfigs.get(config);
        if (tmpHolder != null)
            return tmpHolder;

        FilamentTrimHolder holder = new FilamentTrimHolder();

        trimConfigs.put(config, holder);

        return holder;
    }

    public static void bootstrap(WritableRegistry<TrimPattern> registry) {
        for (Map.Entry<Armor.ArmorConfig, FilamentTrimHolder> entry : trimConfigs.entrySet()) {
            if (entry.getValue() == CHAIN_TRIM && !overwriteChainMail()) continue;

            entry.getValue().trimPattern = register(registry, Items.BARRIER, of(entry.getKey().texture));
        }

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(FilamentTrimPatterns::addRPContents);
    }

    private static void copyChainmailTexture(ResourcePackBuilder resourcePackBuilder) {
        var p1 = "assets/minecraft/textures/models/armor/chainmail_layer_1.png";
        var p2 = "assets/minecraft/textures/models/armor/chainmail_layer_2.png";

        resourcePackBuilder.addData("assets/minecraft/textures/trims/models/armor/chainmail.png", resourcePackBuilder.getDataOrSource(p1));
        resourcePackBuilder.addData("assets/minecraft/textures/trims/models/armor/chainmail_leggings.png", resourcePackBuilder.getDataOrSource(p2));

        BufferedImage image = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        byte[] pngData = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            pngData = stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        resourcePackBuilder.addData(p1, pngData);
        resourcePackBuilder.addData(p2, pngData);
    }

    public static void apply(RegistryAccess registryAccess, ItemStack itemStack) {
        itemStack.set(DataComponents.TRIM, new ArmorTrim(registryAccess.lookup(Registries.TRIM_MATERIAL).get().get(TrimMaterials.QUARTZ).get(), FilamentTrimPatterns.CHAIN_TRIM.trimPattern, false));
    }

    public static void addRPContents(ResourcePackBuilder builder) {
        String path = "assets/minecraft/atlases/armor_trims.json";

        var data = builder.getDataOrSource(path);
        ResourcePackTrimPatternAtlas atlas = Json.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), ResourcePackTrimPatternAtlas.class);

        List<ResourceLocation> resourceLocationList = new ObjectArrayList<>();
        for (Map.Entry<Armor.ArmorConfig, FilamentTrimHolder> entry : trimConfigs.entrySet()) {
            ResourcePackTrimPatternAtlas.Source source = new ResourcePackTrimPatternAtlas.Source();
            source.type = atlas.sources.get(0).type;
            source.palette_key = atlas.sources.get(0).palette_key;
            source.textures = new ObjectArrayList<>();

            if (!resourceLocationList.contains(entry.getKey().texture)) { // only once for every texture
                source.textures.add(entry.getKey().texture.withPrefix("trims/models/armor/").toString());
                source.textures.add(entry.getKey().texture.withPrefix("trims/models/armor/").withSuffix("_leggings").toString());
                resourceLocationList.add(entry.getKey().texture);
            }

            source.permutations = atlas.sources.get(0).permutations;

            atlas.sources.add(source);
        }
        builder.addData(path, Json.GSON.toJson(atlas).getBytes(StandardCharsets.UTF_8));

        if (overwriteChainMail()) {
            copyChainmailTexture(builder);

            List<Item> items = List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
            for (Item item : items) {
                String itemModelPath = "assets/minecraft/models/item/" + BuiltInRegistries.ITEM.getKey(item).getPath() + ".json";
                data = builder.getDataOrSource(itemModelPath);
                // strip overrides, ResourcePackSimplifiedItemModel doesn't have a field for that
                ResourcePackSimplifiedItemModel tmp = Json.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), ResourcePackSimplifiedItemModel.class);
                builder.addData(itemModelPath, Json.GSON.toJson(tmp).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static Holder.Reference<TrimPattern> register(WritableRegistry<TrimPattern> registry, Item template, ResourceKey<TrimPattern> key) {
        TrimPattern armorTrimPattern = new TrimPattern(
                key.location(),
                BuiltInRegistries.ITEM.wrapAsHolder(template),
                Component.empty(),
                false // decal
        );
        return registry.register(key, armorTrimPattern, RegistrationInfo.BUILT_IN);
    }

    private static ResourceKey<TrimPattern> of(ResourceLocation resourceLocation) {
        return ResourceKey.create(Registries.TRIM_PATTERN, resourceLocation);
    }

    public static boolean overwriteChainMail() {
        return FilamentConfig.getInstance().trimArmorReplaceChainmail;
    }

    public static class FilamentTrimHolder {
        public Holder.Reference<TrimPattern> trimPattern;
    }
}