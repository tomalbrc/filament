package de.tomalbrc.filament.data.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemResource {
    private Map<String, ResourceLocation> models = new Object2ObjectArrayMap<>();
    private ResourceLocation parent = ResourceLocation.withDefaultNamespace("item/generated");
    private Map<String, Map<String, ResourceLocation>> textures;

    public static ItemResource of(Map<String, ResourceLocation> models, ResourceLocation parent,  Map<String, Map<String, ResourceLocation>> textures) {
        ItemResource resource = new ItemResource();
        if (models != null) resource.models = models;
        if (parent != null) resource.parent = parent;
        resource.textures = textures;
        return resource;
    }

    public boolean couldGenerate() {
        return models.isEmpty() && this.textures != null;
    }

    public Map<String, Map<String, ResourceLocation>> textures() {
        return textures;
    }

    public ResourceLocation parent() {
        return parent;
    }

    public Map<String, ResourceLocation> models() {
        return models;
    }
}
