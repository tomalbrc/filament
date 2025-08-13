package de.tomalbrc.filament.generator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.util.Json;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.*;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool.FishingRodCastProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool.UsingItemProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric.UseDurationProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.select.ChargeTypeProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.select.CustomModelDataStringProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.special.ShieldSpecialModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.DyeTintSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemAssetGenerator {
    public static void createDefault(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider resourceProvider, boolean tint) {
        var def = resourceProvider.getModels().get("default");
        var defaultModel = new BasicItemModel(def == null ? resourceProvider.getModels().values().iterator().next() : def, !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        if (resourceProvider.getModels().size() > 1) {
            var list = getCases(resourceProvider, tint);
            builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                    new SelectItemModel<>(
                            new SelectItemModel.Switch<>(
                                    new CustomModelDataStringProperty(0),
                                    list
                            ),
                            Optional.of(defaultModel)
                    ), ItemAsset.Properties.DEFAULT).toBytes()
            );
        } else {
            builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                    defaultModel,
                    ItemAsset.Properties.DEFAULT).toBytes()
            );
        }
    }

    @NotNull
    private static ObjectArrayList<SelectItemModel.Case<String>> getCases(ResourceProvider itemResource, boolean tint) {
        var list = new ObjectArrayList<SelectItemModel.Case<String>>();

        for (var modelPathEntry : itemResource.getModels().entrySet()) {
            if (modelPathEntry.getKey().equals("default")) continue;

            var modelId = modelPathEntry.getValue();
            ItemModel model = new BasicItemModel(modelId, !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
            list.add(new SelectItemModel.Case<>(List.of(modelPathEntry.getKey()), model));
        }

        return list;
    }

    public static void createBow(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        var defaultModel = new BasicItemModel(itemResource.getModels().get("default"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_0 = new BasicItemModel(itemResource.getModels().get("pulling_0"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_1 = new BasicItemModel(itemResource.getModels().get("pulling_1"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_2 = new BasicItemModel(itemResource.getModels().get("pulling_2"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));

        var rangeModelBuilder = RangeDispatchItemModel.builder(new UseDurationProperty(false)).scale(0.05f);
        rangeModelBuilder.entry(0.65f, pulling_1);
        rangeModelBuilder.entry(0.9f, pulling_2);
        rangeModelBuilder.fallback(pulling_0);

        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new ConditionItemModel(new UsingItemProperty(), rangeModelBuilder.build(), defaultModel),
                ItemAsset.Properties.DEFAULT).toBytes()
        );
    }

    public static void createCrossbow(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        var defaultModel = new BasicItemModel(itemResource.getModels().get("default"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var rocket = new BasicItemModel(itemResource.getModels().get("rocket"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var arrow = new BasicItemModel(itemResource.getModels().get("arrow"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_0 = new BasicItemModel(itemResource.getModels().get("pulling_0"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_1 = new BasicItemModel(itemResource.getModels().get("pulling_1"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var pulling_2 = new BasicItemModel(itemResource.getModels().get("pulling_2"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFFFF)));

        var rangeModelBuilder = RangeDispatchItemModel.builder(new UseDurationProperty(false)).scale(0.05f);
        rangeModelBuilder.entry(0.58f, pulling_1);
        rangeModelBuilder.entry(1.f, pulling_2);
        rangeModelBuilder.fallback(pulling_0);

        var notUsed = SelectItemModel.builder(new ChargeTypeProperty());
        notUsed.withCase(CrossbowItem.ChargeType.ARROW, arrow);
        notUsed.withCase(CrossbowItem.ChargeType.ROCKET, rocket);
        notUsed.fallback(defaultModel);

        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new ConditionItemModel(new UsingItemProperty(), rangeModelBuilder.build(), notUsed.build()),
                ItemAsset.Properties.DEFAULT).toBytes()
        );
    }

    public static void createShield(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource) {
        var defaultModel = new SpecialItemModel(itemResource.getModels().get("default"), new ShieldSpecialModel());
        var blocking = new SpecialItemModel(itemResource.getModels().get("blocking"), new ShieldSpecialModel());

        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new ConditionItemModel(new UsingItemProperty(), blocking, defaultModel),
                ItemAsset.Properties.DEFAULT).toBytes()
        );
    }

    public static void createFishingRod(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        var defaultModel = new BasicItemModel(itemResource.getModels().get("default"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var cast = new BasicItemModel(itemResource.getModels().get("cast"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new ConditionItemModel(new FishingRodCastProperty(), cast, defaultModel),
                ItemAsset.Properties.DEFAULT).toBytes()
        );
    }

    public static void createTrident(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        var defaultModel = new BasicItemModel(itemResource.getModels().get("default"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        var throwing = new BasicItemModel(itemResource.getModels().get("throwing"), !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));

        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new ConditionItemModel(new UsingItemProperty(), throwing, defaultModel),
                ItemAsset.Properties.DEFAULT).toBytes()
        );
    }

    public static void createTrimModels(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "select");

        JsonArray cases = new JsonArray();
        for (Map.Entry<String, ResourceLocation> entry : itemResource.getModels().entrySet()) {
            if (entry.getKey().equals("default"))
                continue;

            JsonObject caseObj = new JsonObject();
            JsonObject caseModel = new JsonObject();
            caseModel.addProperty("type", "model");
            caseModel.addProperty("model", entry.getValue().withPrefix("item/").toString());

            if (tint) {
                JsonArray tints = new JsonArray();
                JsonObject tintObj = new JsonObject();
                tintObj.addProperty("type", "dye");
                tintObj.addProperty("default", 0xFFFFFF);
                tints.add(tintObj);
                caseModel.add("tints", tints);
            }

            caseObj.add("model", caseModel);
            caseObj.addProperty("when", entry.getKey());
            cases.add(caseObj);
        }
        model.add("cases", cases);

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "model");
        fallback.addProperty("model", itemResource.getModels().get("default").toString());

        if (tint) {
            JsonArray tints = new JsonArray();
            JsonObject tintObj = new JsonObject();
            tintObj.addProperty("type", "dye");
            tintObj.addProperty("default", 0xFFFFFF);
            tints.add(tintObj);
            fallback.add("tints", tints);
        }

        model.add("fallback", fallback);
        model.addProperty("property", "trim_material");

        root.add("model", model);
        String jsonOutput = gson.toJson(root);
        builder.addData(AssetPaths.itemAsset(id), jsonOutput.getBytes(StandardCharsets.UTF_8));
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
}
