package de.tomalbrc.filament.data.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ResourceProvider {
    Map<String, ResourceLocation> getModels();
}
