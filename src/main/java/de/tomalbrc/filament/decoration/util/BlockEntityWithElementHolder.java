package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import net.minecraft.world.level.chunk.LevelChunk;

public interface BlockEntityWithElementHolder {

    void attach(LevelChunk chunk);

    FilamentDecorationHolder getOrCreateHolder();
}
