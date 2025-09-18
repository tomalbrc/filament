package de.tomalbrc.filament.item;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.injection.FilamentItemExtension;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@SuppressWarnings("unused")
public interface FilamentItem extends BehaviourHolder, FilamentItemExtension {
    FilamentItemDelegate getDelegate();

    Data<?> getData();

    default Map<String, ResourceLocation> getModelMap() {
        var resource = this.getData().preferredResource();
        var defaultResource = this.getData().itemResource();
        return resource != null ? resource.getModels() : defaultResource != null ? defaultResource.getModels() : null;
    }

    default FilamentItem asFilamentItem() {
        return this;
    }
}
