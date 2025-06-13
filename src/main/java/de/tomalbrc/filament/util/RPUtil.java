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
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RPUtil {
    public static void create(BehaviourHolder behaviourHolder, Data<?> data) {
        ResourceProvider itemResources = data.itemResource();
        if (data.itemResource() == null && data instanceof BlockData<?> blockData) {
            itemResources = blockData.blockResource();
        }

        if (itemResources instanceof ItemResource ir && ir.couldGenerate()) {
            createItemModels(data.id(), ir);
        }
        if (data instanceof BlockData<?> blockData && blockData.virtual()) {
            createBlockItemAssets(blockData.id(), blockData.blockResource());
        }

        if (itemResources != null && data.itemModel() == null && itemResources.getModels() != null && !data.components().has(DataComponents.ITEM_MODEL)) {
            if (itemResources.getModels().size() > 1) {
                for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : behaviourHolder.getBehaviours()) {
                    if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider) {
                        modelProvider.generate(data);
                        return;
                    }
                }
            }

            // todo: models for "breaking" stage of item (using dur. component) ..? using item behavour + itempredicatemodelprovider?

            ResourceProvider finalItemResources = itemResources;
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
                    object.add("parent", new JsonPrimitive(itemResource.parent().toString()));

                    JsonObject textures = new JsonObject();
                    for (Map.Entry<String, ResourceLocation> stringResourceLocationEntry : entry.getValue().entrySet()) {
                        textures.add(stringResourceLocationEntry.getKey(), new JsonPrimitive(stringResourceLocationEntry.getValue().toString()));
                    }
                    object.add("textures", textures);

                    builder.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                });
                itemResource.getModels().put(entry.getKey(), modelId);
            }
        }
    }
}
