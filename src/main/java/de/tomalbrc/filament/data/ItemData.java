package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("unused")
public final class ItemData extends Data {
    private final @Nullable ItemProperties properties;

    public ItemData(
            @NotNull ResourceLocation id,
            @Nullable Item vanillaItem,
            @Nullable ItemResource itemResource,
            @Nullable BehaviourConfigMap behaviourConfig,
            @Nullable ItemProperties properties,
            @Nullable DataComponentMap components,
            @Nullable ResourceLocation itemGroup
    ) {
        super(id, vanillaItem, itemResource, behaviourConfig, components, itemGroup);
        this.properties = properties;
    }

    @NotNull
    public ItemProperties properties() {
        if (properties == null) {
            return ItemProperties.EMPTY;
        }
        return properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemData) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
