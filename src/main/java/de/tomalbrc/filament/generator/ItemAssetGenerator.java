package de.tomalbrc.filament.generator;

import de.tomalbrc.filament.data.resource.ResourceProvider;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
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

import java.util.List;
import java.util.Optional;

public class ItemAssetGenerator {
    public static void createDefault(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource, boolean tint) {
        var def = itemResource.getModels().get("default");
        var defaultModel = new BasicItemModel(def == null ? itemResource.getModels().values().iterator().next() : def, !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFF)));
        if (itemResource.getModels().size() > 1) {
            var list = getCases(itemResource, tint);
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
}
