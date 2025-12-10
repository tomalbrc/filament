package de.tomalbrc.filament.data.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ItemResource implements ResourceProvider {
    private Map<String, Identifier> models = new Object2ObjectArrayMap<>();
    private Identifier parent;
    private Map<String, Map<String, Identifier>> textures;

    public ItemResource() {}

    public ItemResource(Map<String, Identifier> models, Identifier parent, Map<String, Map<String, Identifier>> textures) {
        this.models = models;
        this.parent = parent;
        this.textures = textures;
    }

    public Identifier parent() {
        if (parent == null)
            parent = Identifier.withDefaultNamespace("item/generated");
        return parent;
    }

    public Map<String, Map<String, Identifier>> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return (models == null || models.isEmpty()) && textures != null;
    }

    @Override
    public Map<String, Identifier> getModels() {
        return this.models;
    }
}
