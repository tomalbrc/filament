package de.tomalbrc.filament.data.behaviours.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block behaviour for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Strippable {
    /**
     * Replacement block
     */
    public ResourceLocation replacement;
}