package de.tomalbrc.filament.data.behaviours.block;

import de.tomalbrc.filament.behaviour.block.BlockBehaviour;

/**
 * Block behaviours for redstone power source
 */
public class Repeater implements BlockBehaviour {
    /**
     * delay in ticks
     */
    public int delay = 0;

    /**
     * power loss during "transfer"
     */
    public int loss = 0;
}