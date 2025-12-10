package de.tomalbrc.filament.data.resource;

import net.minecraft.resources.Identifier;

import java.util.Map;

public interface ResourceProvider {
    Map<String, Identifier> getModels();

    boolean couldGenerate();
}
