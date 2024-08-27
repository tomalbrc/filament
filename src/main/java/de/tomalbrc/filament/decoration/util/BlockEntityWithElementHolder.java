package de.tomalbrc.filament.decoration.util;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public interface BlockEntityWithElementHolder {
    // for already placed decorations
    void attach(LevelChunk chunk);

    // for just-placed ones
    void attach(ServerLevel level);

    ElementHolder makeHolder();

    ElementHolder getDecorationHolder();

    void setDecorationHolder(ElementHolder holder);
}