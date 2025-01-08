package de.tomalbrc.filament.data.resource;

import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record BlockResource(Map<String, PolymerBlockModel> models,
                           Map<String, ResourceLocation> textures,
                           Map<String, ResourceLocation> vanilla) implements ResourceProvider {

    public boolean couldGenerate() {
        return this.textures != null;
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        var map = new Object2ObjectOpenHashMap<String, ResourceLocation>();
        for (Map.Entry<String, PolymerBlockModel> entry : models.entrySet()) {
            var model = entry.getValue().model();
            var itemPath = "item/";
            if (model.getPath().startsWith(itemPath))
                model = model.withPath(model.getPath().substring(0, itemPath.length()));
            map.put(entry.getKey(), model);
        }
        return map;
    }
}
