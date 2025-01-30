package de.tomalbrc.filament.data.resource;

import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record BlockResource(Map<String, PolymerBlockModel> models,
                           ResourceLocation parent,
                           Map<String, PolymerBlockModel> textures,
                           Map<String, ResourceLocation> vanilla) implements ResourceProvider {

    public boolean couldGenerate() {
        return models == null && textures != null && parent != null;
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        var map = new Object2ObjectOpenHashMap<String, ResourceLocation>();
        for (Map.Entry<String, PolymerBlockModel> entry : models.entrySet()) {
            var model = entry.getValue().model();
            map.put(entry.getKey(), model);
        }
        return map;
    }
}
