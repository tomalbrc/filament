package de.tomalbrc.filament.data;

import de.tomalbrc.filament.behaviours.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.util.Constants;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public record ItemData(
        @NotNull ResourceLocation id,
        @Nullable Item vanillaItem,
        @Nullable ItemResource itemResource,
        @Nullable BehaviourConfigMap behaviour,
        @Nullable ItemProperties properties,
        @Nullable DataComponentMap components
) {
    @SuppressWarnings({"FieldCanBeLocal"})

    public Object2ObjectOpenHashMap<String, PolymerModelData> requestModels() {
        Object2ObjectOpenHashMap<String, PolymerModelData> map = new Object2ObjectOpenHashMap<>();
        if (itemResource != null) {
            itemResource.models().forEach((key, value) -> map.put(key, PolymerResourcePackUtils.requestModel(vanillaItem, value)));
        }
        return map.isEmpty() ? null : map;
    }

    public boolean isFood() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.FOOD) != null;
    }

    public boolean isArmor() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.ARMOR) != null;
    }

    public boolean isCosmetic() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.COSMETIC) != null;
    }

    public boolean isFuel() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.FUEL) != null;
    }

    public boolean canShoot() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.SHOOT) != null;
    }
    public boolean isInstrument() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.INSTRUMENT) != null;
    }

    public boolean isTrap() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.TRAP) != null;
    }

    public boolean canExecute() {
        return this.behaviour != null && this.behaviour.get(Constants.Behaviours.EXECUTE) != null;
    }
}
