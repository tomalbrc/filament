package de.tomalbrc.filament.item;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.Data;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface FilamentItem extends BehaviourHolder, Equipable {
    FilamentItemDelegate getDelegate();

    Data<?> getData();

    default Map<String, ResourceLocation> getModelMap() {
        var resource = this.getData().preferredResource();
        var defaultResource = this.getData().itemResource();
        return resource != null ? resource.getModels() : defaultResource != null ? defaultResource.getModels() : null;
    }

    void verifyComponentsAfterLoad(ItemStack itemStack);

    default Item asItem() {
        return (Item) this;
    }

    void requestModels();

    Map<String, PolymerModelData> getModelData();
}
