package de.tomalbrc.filament;

import com.chocohead.mm.api.ClassTinkerers;

public class IShouldNotBeDoingThis implements Runnable {
    @Override
    public void run() {
        ClassTinkerers.enumBuilder("eu.pb4.polymer.blocks.api.BlockModelType").addEnum("TRIPWIRE_BLOCK").build();
        ClassTinkerers.enumBuilder("eu.pb4.polymer.blocks.api.BlockModelType").addEnum("FLAT_TRIPWIRE_BLOCK").build();
        ClassTinkerers.enumBuilder("eu.pb4.polymer.blocks.api.BlockModelType").addEnum("SLAB_BLOCK").build();
    }
}
