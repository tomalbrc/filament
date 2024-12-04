package de.tomalbrc.filament.data.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ItemResource(Map<String, ResourceLocation> models,
                           Map<String, ResourceLocation> textures,
                           Map<String, ResourceLocation> vanilla) implements ResourceProvider {

    public boolean couldGenerate() {
        return this.textures != null && this.textures.containsKey("default");
    }

    @Override
    public Map<String, ResourceLocation> getModels() {
        return this.models;
    }
}
