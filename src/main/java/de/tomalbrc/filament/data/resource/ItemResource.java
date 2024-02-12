package de.tomalbrc.filament.data.resource;

import net.minecraft.resources.ResourceLocation;

public record ItemResource(ResourceLocation model,
                           ResourceLocation texture) {

    public boolean couldGenerate() {
        return this.texture != null;
    }
}
