package de.tomalbrc.filament.data.resource;

import eu.pb4.polymer.blocks.api.PolymerBlockModel;

import java.util.Map;

public record BlockResource(Map<String, PolymerBlockModel> models) {

}
