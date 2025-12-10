package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class ItemData extends Data<ItemProperties> {
    public ItemData(
            @NotNull Identifier id,
            @Nullable Item vanillaItem,
            @Nullable Map<String, String> translations,
            @Nullable Component displayName,
            @Nullable ItemResource itemResource,
            @Nullable Identifier itemModel,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable ItemProperties properties,
            @Nullable DataComponentMap components,
            @Nullable Identifier itemGroup,
            @Nullable Set<Identifier> itemTags
    ) {
        super(id, vanillaItem, translations, displayName, itemResource, itemModel, properties, behaviourConfig, components, itemGroup, itemTags);
    }

    @Override
    @NotNull
    public ItemProperties properties() {
        if (properties == null) {
            properties = new ItemProperties();
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
