package de.tomalbrc.filament.data.resource;

import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record BlockResource(Map<String, PolymerBlockModel> models) {

}
