package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.generator.PreviewModelGenerator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RPUtil {
    private static final Map<Identifier, Consumer<ResourcePackBuilder>> itemAssetGeneratorCallbacks = new ConcurrentHashMap<>();
    private static final Map<Identifier, Consumer<ResourcePackBuilder>> generatedItemCallbacks = new ConcurrentHashMap<>();
    private static final Map<Identifier, List<Consumer<ResourcePackBuilder>>> blockCallbacks = new ConcurrentHashMap<>();
    private static final Map<Identifier, Consumer<ResourcePackBuilder>> extraItemCallbacks = new ConcurrentHashMap<>();
    private static final Map<Identifier, Consumer<ResourcePackBuilder>> virtualBlockItemCallbacks = new ConcurrentHashMap<>();

    public static void addExtraAssets(ResourcePackBuilder builder) {
        generatedItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        blockCallbacks.forEach((_, list) -> list.forEach(x -> x.accept(builder)));
        virtualBlockItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        extraItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        itemAssetGeneratorCallbacks.forEach((_, consumer) -> consumer.accept(builder));
    }

    public static void create(BehaviourHolder behaviourHolder, Data<?> data) {
        ResourceProvider resource = resolveResource(data);

        if (data instanceof AbstractBlockData<?> blockData) {
            if (blockData.properties().virtual() || blockData instanceof DecorationData) {
                createBlockItemAssets(blockData.id(), blockData.blockResource());
            }
            createBlockModels(blockData.id(), blockData.blockResource());
        }

        if (!shouldGenerateItemAsset(data, resource)) return;
        if (tryModelProvider(behaviourHolder, data, resource)) return;

        if (resource instanceof ItemResource ir) {
            generateItemModels(data.id(), ir);
        }

        boolean preview = FilamentConfig.getInstance().decorationPlacementPreviews && data instanceof DecorationData;

        itemAssetGeneratorCallbacks.put(data.id(), builder -> {
            boolean tint = isTinted(data);
            if (preview)
                    PreviewModelGenerator.createWithPreviewVariants(builder, data.id(), resource, tint);
                else
                    ItemAssetGenerator.createDefault(builder, data.id(), resource, tint);
        });
    }

    private static ResourceProvider resolveResource(Data<?> data) {
        if (data.itemResource() != null) return data.itemResource();
        if (data instanceof AbstractBlockData<?> blockData) return blockData.blockResource();
        return null;
    }

    private static boolean shouldGenerateItemAsset(Data<?> data, ResourceProvider resource) {
        if (resource == null) return false;
        if (data.components().has(DataComponents.ITEM_MODEL)) return false;
        if (data.itemModel() != null) return false;
        return resource.getModels() != null || resource.couldGenerate();
    }

    private static boolean tryModelProvider(BehaviourHolder holder, Data<?> data, ResourceProvider resource) {
        var behaviours = holder.getBehaviours();
        if (behaviours == null || behaviours.isEmpty()) return false;

        for (var behaviour : behaviours) {
            if (!(behaviour instanceof ItemPredicateModelProvider provider)) continue;
            if (!provider.hasRequiredModels(data)) continue;

            if (resource instanceof ItemResource ir && !provider.canCreateItemModels()) {
                generateItemModels(data.id(), ir);
            }

            provider.generate(data);
            return true;
        }
        return false;
    }

    private static boolean isTinted(Data<?> data) {
        return data.components().has(DataComponents.DYED_COLOR) || isDyable(data.vanillaItem());
    }

    public static boolean isDyable(Item item) {
        // 26.1 doesnt have the dyed_color component on items at the time the assets are generated (too early)
        // so we hardcode the check here to keep compat with existing filament configs
        return item.components().has(DataComponents.DYED_COLOR) || item == Items.LEATHER_HORSE_ARMOR || item == Items.LEATHER_BOOTS || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_HELMET || item == Items.FIREWORK_STAR;
    }

    // Item assets for virtual blocks that use item displays (NOT DECORATIONS!)
    public static void createBlockItemAssets(Identifier id, BlockResource blockResource) {
        if (blockResource == null) return;

        virtualBlockItemCallbacks.put(id, builder ->
                ItemAssetGenerator.createDefault(builder, id.withPrefix("block/"), blockResource, false)
        );
    }

    private static void createBlockModels(Identifier id, BlockResource blockResource) {
        if (blockResource == null || !blockResource.couldGenerate()) return;

        int index = 1;
        Map<Map<String, Identifier>, Identifier> localCache = new Object2ObjectOpenHashMap<>();
        List<Consumer<ResourcePackBuilder>> consumers = new ArrayList<>();

        for (var entry : blockResource.textures().entrySet()) {
            Identifier model = localCache.get(entry.getValue().textures());
            if (model == null) {
                model = id.withPrefix("block/").withSuffix("_" + index);
                localCache.put(entry.getValue().textures(), model);

                final Identifier modelId = model;
                consumers.add(builder -> writeModelJson(
                        builder,
                        modelId,
                        blockResource.parent(),
                        entry.getValue().textures()
                ));
            }

            blockResource.addModel(entry.getKey(), PolymerBlockModel.of(
                    model,
                    entry.getValue().x(),
                    entry.getValue().y(),
                    entry.getValue().uvlock(),
                    entry.getValue().weight()
            ));
            index++;
        }

        blockCallbacks.put(id, consumers);
    }

    public static void addExtraGenerator(@NotNull Identifier id, Consumer<ResourcePackBuilder> generator) {
        extraItemCallbacks.put(id, generator);
    }

    /**
     * Generates item models from an ItemResource object (if possible)
     * @param id Root id for the generated models' paths
     * @param itemResource
     */
    public static void generateItemModels(Identifier id, ItemResource itemResource) {
        if (!itemResource.couldGenerate()) return;

        for (var entry : itemResource.textures().entrySet()) {
            Identifier modelId = id.withPrefix("item/").withSuffix("_" + entry.getKey());

            generatedItemCallbacks.put(modelId, builder -> writeModelJson(
                    builder,
                    modelId,
                    itemResource.parent(),
                    entry.getValue()
            ));

            itemResource.getModels().put(entry.getKey(), modelId);
        }
    }

    private static void writeModelJson(ResourcePackBuilder builder, Identifier modelId, Identifier parent, Map<String, Identifier> textures) {
        JsonObject object = new JsonObject();
        object.add("parent", new JsonPrimitive(shortId(parent)));

        JsonObject texturesObj = new JsonObject();
        for (var texture : textures.entrySet()) {
            texturesObj.add(texture.getKey(), new JsonPrimitive(shortId(texture.getValue())));
        }
        object.add("textures", texturesObj);

        builder.addData(
                "assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json",
                Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8)
        );
    }

    // to keep jsons as small as possible strip default namespace
    private static String shortId(Identifier id) {
        return id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? id.getPath() : id.toString();
    }
}