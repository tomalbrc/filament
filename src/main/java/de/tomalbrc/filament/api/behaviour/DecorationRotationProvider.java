package de.tomalbrc.filament.api.behaviour;

import net.minecraft.world.level.block.state.BlockState;

public interface DecorationRotationProvider {
    float getVisualRotationYInDegrees(BlockState blockState);
}
