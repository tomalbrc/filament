package de.tomalbrc.filament.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.Json;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ItemAssetGenerator {
    public static void createBow(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource, boolean dye) {
        var defaultModel = itemResource.getModels().get("default");
        var pulling_0 = itemResource.getModels().get("pulling_0");
        var pulling_1 = itemResource.getModels().get("pulling_1");
        var pulling_2 = itemResource.getModels().get("pulling_2");

        JsonObject bowModel = new JsonObject();
        bowModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride(1, pulling_0.toString(), null));
        overrides.add(createOverride(1, pulling_1.toString(), 0.65));
        overrides.add(createOverride(1, pulling_2.toString(), 0.9));

        bowModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), bowModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void createCrossbow(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource, boolean dye) {
        var defaultModel = itemResource.getModels().get("default");
        var rocket = itemResource.getModels().get("rocket");
        var arrow = itemResource.getModels().get("arrow");
        var pulling_0 = itemResource.getModels().get("pulling_0");
        var pulling_1 = itemResource.getModels().get("pulling_1");
        var pulling_2 = itemResource.getModels().get("pulling_2");

        JsonObject crossbowModel = new JsonObject();
        crossbowModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride(1, pulling_0.toString(), null));
        overrides.add(createOverride(1, pulling_1.toString(), 0.58));
        overrides.add(createOverride(1, pulling_2.toString(), 1.0));
        overrides.add(createOverride("charged", rocket.toString(), 1));
        overrides.add(createOverride("charged", arrow.toString(), 1));

        crossbowModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), crossbowModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void createShield(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource) {
        var defaultModel = itemResource.getModels().get("default");
        var blocking = itemResource.getModels().get("blocking");

        JsonObject shieldModel = new JsonObject();
        shieldModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride("blocking", blocking.toString(), 1));

        shieldModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), shieldModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void createFishingRod(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource, boolean dye) {
        var defaultModel = itemResource.getModels().get("default");
        var cast = itemResource.getModels().get("cast");

        JsonObject rodModel = new JsonObject();
        rodModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride("cast", cast.toString(), 1));

        rodModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), rodModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static JsonObject createOverride(int pulling, String model, Double pull) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty("pulling", pulling);
        if (pull != null) {
            predicate.addProperty("pull", pull);
        }

        JsonObject override = new JsonObject();
        override.add("predicate", predicate);
        override.addProperty("model", model);

        return override;
    }

    private static JsonObject createOverride(String predicateKey, String model, int value) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty(predicateKey, value);

        JsonObject override = new JsonObject();
        override.add("predicate", predicate);
        override.addProperty("model", model);

        return override;
    }

    public static void createItemModels(ResourceLocation id, ItemResource itemResource) {
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

    public static void createTrident(ResourcePackBuilder resourcePackBuilder, @NotNull ResourceLocation id, ItemResource itemResource, boolean b) {
        // TODO: 1.21.1 maybe?
    }
}