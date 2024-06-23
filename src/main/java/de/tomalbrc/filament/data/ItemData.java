package de.tomalbrc.filament.data;

import de.tomalbrc.filament.data.behaviours.item.ItemBehaviourList;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.ItemResource;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("unused")
public record ItemData(
        @NotNull ResourceLocation id,
        @Nullable Item vanillaItem,
        @Nullable ItemResource itemResource,
        @Nullable ItemBehaviourList behaviour,
        @Nullable ItemProperties properties
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
        return this.behaviour != null && this.behaviour.food != null;
    }

    public boolean isArmor() {
        return this.behaviour != null && this.behaviour.armor != null;
    }

    public boolean isCosmetic() {
        return this.behaviour != null && this.behaviour.cosmetic != null;
    }

    public boolean isFuel() {
        return this.behaviour != null && this.behaviour.fuel != null;
    }

    public boolean canShoot() {
        return this.behaviour != null && this.behaviour.shoot != null;
    }
    public boolean isInstrument() {
        return this.behaviour != null && this.behaviour.instrument != null && this.behaviour.instrument.sound != null;
    }

    public boolean isTrap() {
        return this.behaviour != null && this.behaviour.trap != null && this.behaviour.trap.types != null;
    }

    public boolean canExecute() {
        return this.behaviour != null && this.behaviour.execute != null;
    }
}
