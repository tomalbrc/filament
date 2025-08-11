package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public interface ItemPredicateModelProvider {
    default boolean canCreateItemModels() {
        return false;
    }

    void generate(Data<?> data);

    List<String> requiredModels();

    default boolean hasRequiredModels(Data<?> data) {
        return hasRequired(data.id(), data.itemResource(), requiredModels());
    }

    private static boolean hasRequired(ResourceLocation info, ItemResource resource, Collection<String> req) {
        for (String string : req) {
            if ((resource.getModels() == null || !resource.getModels().containsKey(string)) && (resource.textures() == null || !resource.textures().containsKey(string))) {
                Filament.LOGGER.error("Could not generate item asset model for '{}' - requires the following 'models' / 'textures' object keys: {}", info, req);
                return false;
            }
        }

        return true;
    }
}
