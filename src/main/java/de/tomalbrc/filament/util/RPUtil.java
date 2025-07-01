package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RPUtil {
    public static void create(BehaviourHolder behaviourHolder, Data<?> data) {
        ResourceProvider resource = data.itemResource();
        if (data instanceof BlockData blockData) {
            if (data.itemResource() == null) resource = blockData.blockResource();

            if (blockData.properties().virtual)
                createBlockItemAssets(blockData.id(), blockData.blockResource());

            createBlockModels(blockData.id(), blockData.blockResource());
        }

        if (resource instanceof ItemResource ir) {
            createItemModels(data.id(), ir);
        }

        if (resource != null && data.itemModel() == null && resource.getModels() != null && !data.components().has(DataComponents.ITEM_MODEL)) {
            if (resource.getModels().size() > 1) {
                for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : behaviourHolder.getBehaviours()) {
                    if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider) {
                        modelProvider.generate(data);
                        return;
                    }
                }
            }

            ResourceProvider finalItemResources = resource;
            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder ->
                    ItemAssetGenerator.createDefault(
                            resourcePackBuilder,
                            data.id(),
                            finalItemResources,
                            data.components().has(DataComponents.DYED_COLOR) || data.vanillaItem().components().has(DataComponents.DYED_COLOR)
                    )
            );
        }
    }

    // Item assets for virtual blocks that use item displays (NOT DECORATIONS!)
    public static void createBlockItemAssets(ResourceLocation id, BlockResource blockResource) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder ->
                ItemAssetGenerator.createDefault(
                        resourcePackBuilder,
                        id.withPrefix("block/"),
                        blockResource,
                        false
                )
        );
    }

    private static void createItemModels(ResourceLocation id, ItemResource itemResource) {
        if (itemResource.couldGenerate()) {
            for (Map.Entry<String, Map<String, ResourceLocation>> entry : itemResource.textures().entrySet()) {
                final var modelId = id.withPrefix("item/").withSuffix("_" + entry.getKey());
                PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(builder -> {
                    JsonObject object = new JsonObject();
                    object.add("parent", new JsonPrimitive(itemResource.parent().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? itemResource.parent().getPath() : itemResource.parent().toString()));

                    JsonObject textures = new JsonObject();
                    for (Map.Entry<String, ResourceLocation> texturesMapEntry : entry.getValue().entrySet()) {
                        textures.add(texturesMapEntry.getKey(), new JsonPrimitive(texturesMapEntry.getValue().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? texturesMapEntry.getValue().getPath() : texturesMapEntry.getValue().toString()));
                    }
                    object.add("textures", textures);

                    builder.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                });
                itemResource.getModels().put(entry.getKey(), modelId);
            }
        }
    }

    private static void createBlockModels(ResourceLocation id, BlockResource blockResource) {
        if (blockResource.couldGenerate()) {
            int index = 1;
            Map<Map<String, ResourceLocation>, ResourceLocation> localCache = new Object2ObjectOpenHashMap<>();

            for (Map.Entry<String, BlockResource.TextureBlockModel> entry : blockResource.textures().entrySet()) {
                var model = id.withPrefix("block/").withSuffix("_" + index);
                if (localCache.containsKey(entry.getValue().textures())) {
                    model = localCache.get(entry.getValue().textures());
                } else {
                    localCache.put(entry.getValue().textures(), model);

                    final var modelId = model;
                    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(builder -> {
                        JsonObject object = new JsonObject();
                        object.add("parent", new JsonPrimitive(blockResource.parent().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? blockResource.parent().getPath() : blockResource.parent().toString()));

                        JsonObject textures = new JsonObject();
                        for (Map.Entry<String, ResourceLocation> texturesMapEntry : entry.getValue().textures().entrySet()) {
                            textures.add(texturesMapEntry.getKey(), new JsonPrimitive(texturesMapEntry.getValue().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? texturesMapEntry.getValue().getPath() : texturesMapEntry.getValue().toString()));
                        }
                        object.add("textures", textures);

                        builder.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                    });
                }

                blockResource.addModel(entry.getKey(), PolymerBlockModel.of(model, entry.getValue().x(), entry.getValue().y(), entry.getValue().uvLock(), entry.getValue().weight()));
                index++;
            }
        }
    }
}
