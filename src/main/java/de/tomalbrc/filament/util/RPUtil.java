package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RPUtil {
    static Map<Identifier, Consumer<ResourcePackBuilder>> itemAssetGeneratorCallbacks = new ConcurrentHashMap<>();
    static Map<Identifier, Consumer<ResourcePackBuilder>> generatedItemCallbacks = new ConcurrentHashMap<>();
    static Map<Identifier, List<Consumer<ResourcePackBuilder>>> blockCallbacks = new ConcurrentHashMap<>();
    static Map<Identifier, Consumer<ResourcePackBuilder>> extraItemCallbacks = new ConcurrentHashMap<>();
    static Map<Identifier, Consumer<ResourcePackBuilder>> virtualBlockItemCallbacks = new ConcurrentHashMap<>();

    public static void addExtraAssets(ResourcePackBuilder builder) {
        generatedItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        blockCallbacks.forEach((_, list) -> list.forEach(x -> x.accept(builder)));
        virtualBlockItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        extraItemCallbacks.forEach((_, consumer) -> consumer.accept(builder));
        itemAssetGeneratorCallbacks.forEach((_, consumer) -> consumer.accept(builder));
    }

    public static void create(BehaviourHolder behaviourHolder, Data<?> data) {
        ResourceProvider resource = data.itemResource();
        if (data instanceof AbstractBlockData<?> blockData) {
            if (data.itemResource() == null && blockData.blockResource() != null) resource = blockData.blockResource();

            if (blockData.properties().virtual() || blockData instanceof DecorationData)
                createBlockItemAssets(blockData.id(), blockData.blockResource());

            createBlockModels(blockData.id(), blockData.blockResource());
        }

        if (resource != null && !data.components().has(DataComponents.ITEM_MODEL) && data.itemModel() == null && (resource.getModels() != null || resource.couldGenerate())) {
            if (behaviourHolder.getBehaviours() != null && !behaviourHolder.getBehaviours().isEmpty()) {
                for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : behaviourHolder.getBehaviours()) {
                    if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider && modelProvider.hasRequiredModels(data)) {
                        if (resource instanceof ItemResource ir && !modelProvider.canCreateItemModels()) {
                            generateItemModels(data.id(), ir);
                        }

                        modelProvider.generate(data);
                        return;
                    }
                }
            }

            if (resource instanceof ItemResource ir) {
                generateItemModels(data.id(), ir);
            }

            ResourceProvider finalItemResources = resource;
            itemAssetGeneratorCallbacks.put(data.id(), resourcePackBuilder -> {
                ItemAssetGenerator.createDefault(
                        resourcePackBuilder,
                        data.id(),
                        finalItemResources,
                        data.components().has(DataComponents.DYED_COLOR) || data.vanillaItem().components().has(DataComponents.DYED_COLOR)
                );
            });
        }
    }

    // Item assets for virtual blocks that use item displays (NOT DECORATIONS!)
    public static void createBlockItemAssets(Identifier id, BlockResource blockResource) {
        if (blockResource != null) virtualBlockItemCallbacks.put(id, resourcePackBuilder -> {
            ItemAssetGenerator.createDefault(
                    resourcePackBuilder,
                    id.withPrefix("block/"),
                    blockResource,
                    false
            );
        });
    }

    private static void createBlockModels(Identifier id, BlockResource blockResource) {
        if (blockResource != null && blockResource.couldGenerate()) {
            int index = 1;
            Map<Map<String, Identifier>, Identifier> localCache = new Object2ObjectOpenHashMap<>();

            List<Consumer<ResourcePackBuilder>> consumers = new ArrayList<>();

            for (Map.Entry<String, BlockResource.TextureBlockModel> entry : blockResource.textures().entrySet()) {
                var model = id.withPrefix("block/").withSuffix("_" + index);
                if (localCache.containsKey(entry.getValue().textures())) {
                    model = localCache.get(entry.getValue().textures());
                } else {
                    localCache.put(entry.getValue().textures(), model);

                    final var modelId = model;
                    consumers.add(builder -> {
                        JsonObject object = new JsonObject();
                        object.add("parent", new JsonPrimitive(blockResource.parent().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? blockResource.parent().getPath() : blockResource.parent().toString()));

                        JsonObject textures = new JsonObject();
                        for (Map.Entry<String, Identifier> texturesMapEntry : entry.getValue().textures().entrySet()) {
                            textures.add(texturesMapEntry.getKey(), new JsonPrimitive(texturesMapEntry.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? texturesMapEntry.getValue().getPath() : texturesMapEntry.getValue().toString()));
                        }
                        object.add("textures", textures);

                        builder.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                    });
                }

                blockResource.addModel(entry.getKey(), PolymerBlockModel.of(model, entry.getValue().x(), entry.getValue().y(), entry.getValue().uvlock(), entry.getValue().weight()));
                index++;
            }

            blockCallbacks.put(id, consumers);
        }
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
        if (itemResource.couldGenerate()) {
            for (Map.Entry<String, Map<String, Identifier>> entry : itemResource.textures().entrySet()) {
                final var modelId = id.withPrefix("item/").withSuffix("_" + entry.getKey());

                generatedItemCallbacks.put(modelId, builder -> {
                    JsonObject object = new JsonObject();
                    object.add("parent", new JsonPrimitive(itemResource.parent().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? itemResource.parent().getPath() : itemResource.parent().toString()));

                    JsonObject textures = new JsonObject();
                    for (Map.Entry<String, Identifier> texturesMapEntry : entry.getValue().entrySet()) {
                        textures.add(texturesMapEntry.getKey(), new JsonPrimitive(texturesMapEntry.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? texturesMapEntry.getValue().getPath() : texturesMapEntry.getValue().toString()));
                    }
                    object.add("textures", textures);

                    builder.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                });

                itemResource.getModels().put(entry.getKey(), modelId);
            }
        }
    }
}
