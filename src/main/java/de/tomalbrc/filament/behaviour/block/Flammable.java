package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for flammable blocks and decorations. (Uses filament registration events + fabric flammable registry)
 */
public class Flammable implements BlockBehaviour<Flammable.Config> {
    private final Config config;

    public Flammable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Flammable.Config getConfig() {
        return this.config;
    }

    public static class Config {
        public int spread;
        public int burn;
    }
}