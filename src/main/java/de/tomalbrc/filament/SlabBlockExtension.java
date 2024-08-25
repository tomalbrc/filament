package de.tomalbrc.filament;

import com.chocohead.mm.api.ClassTinkerers;

public class SlabBlockExtension implements Runnable {
    @Override
    public void run() {
        ClassTinkerers.enumBuilder("eu.pb4.polymer.blocks.api.BlockModelType").addEnum("SLAB_BLOCK").build();
    }
}
