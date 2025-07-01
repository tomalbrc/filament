package de.tomalbrc.filament.data.resource;

import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class BlockResource implements ResourceProvider {
    private Map<String, PolymerBlockModel> models = new Object2ObjectArrayMap<>();
    private ResourceLocation parent = ResourceLocation.withDefaultNamespace("block/cube_all");
    private Map<String, TextureBlockModel> textures;

    public BlockResource(Map<String, PolymerBlockModel> models) {
        this(models, null, null);
    }

    public BlockResource(Map<String, PolymerBlockModel> models, ResourceLocation parent, Map<String, TextureBlockModel> textures) {
        this.models = models;
        this.parent = parent;
        this.textures = textures;
    }

    public ResourceLocation parent() {
        return parent;
    }

    public Map<String, TextureBlockModel> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return (models == null || models.isEmpty()) && textures != null && parent != null;
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        var map = new Object2ObjectOpenHashMap<String, ResourceLocation>();
        if (this.models == null) {
            this.models = new Object2ObjectArrayMap<>();
            return map;
        }

        for (Map.Entry<String, PolymerBlockModel> entry : models.entrySet()) {
            var model = entry.getValue().model();
            map.put(entry.getKey(), model);
        }
        return map;
    }

    public Map<String, PolymerBlockModel> models() {
        return this.models;
    }

    public void addModel(String key, PolymerBlockModel blockModel) {
        if (this.models == null) this.models = new Object2ObjectArrayMap<>();
        this.models.put(key, blockModel);
    }


    public record TextureBlockModel(Map<String, ResourceLocation> textures, int x, int y, boolean uvLock, int weight) {}
}