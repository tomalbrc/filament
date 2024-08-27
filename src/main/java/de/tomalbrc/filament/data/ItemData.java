package de.tomalbrc.filament.data;

import com.google.gson.annotations.SerializedName;
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
        @SerializedName("behaviour")
        @Nullable BehaviourConfigMap behaviourConfig,
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
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.FOOD) != null;
    }

    public boolean isArmor() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.ARMOR) != null;
    }

    public boolean isCosmetic() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.COSMETIC) != null;
    }

    public boolean isFuel() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.FUEL) != null;
    }

    public boolean canShoot() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.SHOOT) != null;
    }
    public boolean isInstrument() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.INSTRUMENT) != null;
    }

    public boolean isTrap() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.TRAP) != null;
    }

    public boolean canExecute() {
        return this.behaviourConfig != null && this.behaviourConfig.get(Constants.Behaviours.EXECUTE) != null;
    }
}
