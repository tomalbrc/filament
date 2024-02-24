package de.tomalbrc.filament.data.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ItemResource(Map<String, ResourceLocation> models,
                           Map<String, ResourceLocation> textures,
                           Map<String, ResourceLocation> vanilla) {

    public boolean couldGenerate() {
        return this.textures != null && this.textures.containsKey("default");
    }
}
