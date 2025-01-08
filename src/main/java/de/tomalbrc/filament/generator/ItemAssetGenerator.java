package de.tomalbrc.filament.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.filament.data.resource.ItemResource;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

public class ItemAssetGenerator {
    public static void createBow(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource) {
        var defaultModel = itemResource.models().get("default");
        var pulling_0 = itemResource.models().get("pulling_0");
        var pulling_1 = itemResource.models().get("pulling_1");
        var pulling_2 = itemResource.models().get("pulling_2");

        JsonObject bowModel = new JsonObject();
        bowModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride(1, pulling_0.toString(), null));
        overrides.add(createOverride(1, pulling_1.toString(), 0.65));
        overrides.add(createOverride(1, pulling_2.toString(), 0.9));

        bowModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), bowModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void createCrossbow(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource) {
        var defaultModel = itemResource.models().get("default");
        var rocket = itemResource.models().get("rocket");
        var arrow = itemResource.models().get("arrow");
        var pulling_0 = itemResource.models().get("pulling_0");
        var pulling_1 = itemResource.models().get("pulling_1");
        var pulling_2 = itemResource.models().get("pulling_2");

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
        var defaultModel = itemResource.models().get("default");
        var blocking = itemResource.models().get("blocking");

        JsonObject shieldModel = new JsonObject();
        shieldModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride("blocking", blocking.toString(), 1));

        shieldModel.add("overrides", overrides);

        builder.addData(AssetPaths.itemModel(id), shieldModel.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void createFishingRod(ResourcePackBuilder builder, ResourceLocation id, ItemResource itemResource) {
        var defaultModel = itemResource.models().get("default");
        var cast = itemResource.models().get("cast");

        JsonObject rodModel = new JsonObject();
        rodModel.addProperty("parent", defaultModel.toString()); // Use "default" as parent

        JsonArray overrides = new JsonArray();

        overrides.add(createOverride(1, cast.toString(), null));

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
}
