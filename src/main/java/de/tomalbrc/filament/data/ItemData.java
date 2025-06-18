package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class ItemData extends Data<ItemProperties> {
    transient private final ItemProperties EMPTY = new ItemProperties();

    public ItemData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
            @Nullable ResourceLocation itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable ItemProperties properties,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup,
            @Nullable Set<ResourceLocation> itemTags
    ) {
        super(id, vanillaItem, translations, displayName, itemResource, itemModel, properties, behaviourConfig, components, itemGroup, itemTags);
    }

    @Override
    @NotNull
    public ItemProperties properties() {
        if (properties == null) {
            return EMPTY;
        }
        return properties;
    }

    @Override
    public String toString() {
        return "ItemData[" +
                "id=" + id + ", " +
                "vanillaItem=" + vanillaItem + ", " +
                "itemResource=" + itemResource + ", " +
                "behaviourConfig=" + behaviour + ", " +
                "properties=" + properties + ", " +
                "components=" + components + ", " +
                "itemGroup=" + group + ']';
    }
}
