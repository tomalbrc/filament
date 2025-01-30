package de.tomalbrc.filament.data.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ItemResource implements ResourceProvider {
    private final Map<String, ResourceLocation> models = new Object2ObjectArrayMap<>();
    private ResourceLocation parent;
    private Map<String, Map<String, ResourceLocation>> textures;

    public ResourceLocation parent() {
        return parent;
    }

    public Map<String, Map<String, ResourceLocation>> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return models.isEmpty() && textures != null && parent != null;
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        return this.models;
    }
}
