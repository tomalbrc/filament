package de.tomalbrc.filament.generator;

import de.tomalbrc.filament.data.resource.ResourceProvider;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.ItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.SelectItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.select.CustomModelDataStringProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.DyeTintSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ItemAssetGenerator {
    public static void create(ResourcePackBuilder builder, ResourceLocation id, ResourceProvider itemResource) {
        var def = itemResource.getModels().get("default");
        var defaultModel = new BasicItemModel(def == null ? itemResource.getModels().values().iterator().next() : def, List.of(new DyeTintSource(0xFFFFFF)));
        if (itemResource.getModels().size() > 1) {
            var list = getCases(itemResource);
            builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                    new SelectItemModel<>(
                            new SelectItemModel.Switch<>(
                                    new CustomModelDataStringProperty(0),
                                    list
                            ),
                            Optional.of(defaultModel)
                    ), ItemAsset.Properties.DEFAULT).toJson().getBytes(StandardCharsets.UTF_8)
            );
        } else {
            builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                    defaultModel,
                    ItemAsset.Properties.DEFAULT).toJson().getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    @NotNull
    private static ObjectArrayList<SelectItemModel.Case<String>> getCases(ResourceProvider itemResource) {
        var list = new ObjectArrayList<SelectItemModel.Case<String>>();

        for (var modelPathEntry : itemResource.getModels().entrySet()) {
            if (modelPathEntry.getKey().equals("default")) continue;

            var modelId = modelPathEntry.getValue();
            ItemModel model = new BasicItemModel(modelId, List.of(new DyeTintSource(0xFFFFFF)));
            list.add(new SelectItemModel.Case<>(List.of(modelPathEntry.getKey()), model));
        }

        return list;
    }
}
