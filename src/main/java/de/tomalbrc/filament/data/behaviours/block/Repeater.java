package de.tomalbrc.filament.data.behaviours.block;

import de.tomalbrc.filament.behaviour.block.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

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