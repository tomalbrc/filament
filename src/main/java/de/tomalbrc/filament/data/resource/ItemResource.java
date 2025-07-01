package de.tomalbrc.filament.data.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ItemResource implements ResourceProvider {
    private Map<String, ResourceLocation> models = new Object2ObjectArrayMap<>();
    private ResourceLocation parent = ResourceLocation.withDefaultNamespace("item/generated");
    private Map<String, Map<String, ResourceLocation>> textures;

    public ItemResource() {}

    public ItemResource(Map<String, ResourceLocation> models, ResourceLocation parent, Map<String, Map<String, ResourceLocation>> textures) {
        this.models = models;
        this.parent = parent;
        this.textures = textures;
    }

    public ResourceLocation parent() {
        return parent;
    }

    public Map<String, Map<String, ResourceLocation>> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return (models == null || models.isEmpty()) && textures != null && parent != null;
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        return this.models;
    }
}
