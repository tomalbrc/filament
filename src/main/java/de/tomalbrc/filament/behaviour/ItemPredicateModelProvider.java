package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.data.resource.ItemResource;
import net.minecraft.resources.ResourceLocation;

public interface ItemPredicateModelProvider {
    void generate(ResourceLocation id, ItemResource itemResource);
}
