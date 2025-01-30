package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.item.SimpleItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RPUtil {
    public static void create(SimpleItem item, ResourceLocation id, ItemResource itemResource) {
        if (itemResource != null && itemResource.models() != null && itemResource.models().size() > 1 && !item.components().has(DataComponents.CUSTOM_MODEL_DATA)) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : item.getBehaviours()) {
                if (entry.getValue() instanceof ItemPredicateModelProvider modelProvider) {
                    modelProvider.generate(id, itemResource);
                    return;
                }
            }
        }
    }

    public static boolean useGeneratedModel(BehaviourMap behaviourMap) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : behaviourMap) {
            if (entry.getValue() instanceof ItemPredicateModelProvider) {
                return true;
            }
        }
        return false;
    }

    public static void createItemModels(ResourceLocation id, ItemResource itemResource) {
        if (itemResource.couldGenerate()) {
            for (Map.Entry<String, Map<String, ResourceLocation>> entry : itemResource.textures().entrySet()) {
                final var modelId = id.withPrefix("item/").withSuffix("_" + entry.getKey());
                PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(x -> {
                    JsonObject object = new JsonObject();
                    object.add("parent", new JsonPrimitive(itemResource.parent().toString()));

                    JsonObject textures = new JsonObject();
                    for (Map.Entry<String, ResourceLocation> stringResourceLocationEntry : entry.getValue().entrySet()) {
                        textures.add(stringResourceLocationEntry.getKey(), new JsonPrimitive(stringResourceLocationEntry.getValue().toString()));
                    }
                    object.add("textures", textures);

                    x.addData("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json", Json.GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
                });
                itemResource.models().put(entry.getKey(), modelId);
            }
        }
    }
}
