package de.tomalbrc.filament.data.behaviours.block;

import de.tomalbrc.filament.behaviour.block.BlockBehaviour;
import net.minecraft.resources.ResourceLocation;

/**
 * Block behaviour for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Strippable implements BlockBehaviour {
    /**
     * Replacement block
     */
    public ResourceLocation replacement;
}