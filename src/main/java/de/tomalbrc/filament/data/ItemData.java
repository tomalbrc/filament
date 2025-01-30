package de.tomalbrc.filament.data;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.RPUtil;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public record ItemData(
        @NotNull ResourceLocation id,
        @Nullable Item vanillaItem,
        @Nullable Map<String, String> translations,
        @Nullable ItemResource itemResource,
        @SerializedName("behaviour")
        @Nullable BehaviourConfigMap behaviourConfig,
        @Nullable ItemProperties properties,
        @Nullable DataComponentMap components,
        @SerializedName("group")
        @Nullable ResourceLocation itemGroup
) {
    @Override
    @NotNull
    public ItemProperties properties() {
        if (properties == null) {
            return ItemProperties.EMPTY;
        }
        return properties;
    }

    @Override
    @NotNull
    public DataComponentMap components() {
        if (components == null) {
            return DataComponentMap.EMPTY;
        }
        return components;
    }

    @Override
    @NotNull
    public Item vanillaItem() {
        if (vanillaItem == null) {
            return Items.PAPER;
        }
        return vanillaItem;
    }

    public Object2ObjectOpenHashMap<String, PolymerModelData> requestModels(BehaviourMap behaviourMap) {
        if (itemResource != null && itemResource.couldGenerate()) {
            RPUtil.createItemModels(id(), Objects.requireNonNull(itemResource));
        }

        Object2ObjectOpenHashMap<String, PolymerModelData> map = new Object2ObjectOpenHashMap<>();
        if (itemResource != null) {
            if (itemResource.models().size() > 1 && RPUtil.useGeneratedModel(behaviourMap)) {
                map.put("default", PolymerResourcePackUtils.requestModel(this.vanillaItem == null ? Items.PAPER : this.vanillaItem, id().withPrefix("item/")));
            } else {
                itemResource.models().forEach((key, value) -> map.put(key, PolymerResourcePackUtils.requestModel(this.vanillaItem == null ? Items.PAPER : this.vanillaItem, value)));
            }
        }
        return map.isEmpty() ? null : map;
    }
}
