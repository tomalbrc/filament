package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.block.BlockBehaviour;

/**
 * Block behaviours for redstone power source
 */
public class Repeater implements BlockBehaviour<Repeater.RepeaterConfig> {
    private final RepeaterConfig config;

    public Repeater(RepeaterConfig config) {
        this.config = config;
    }

    @Override
    public RepeaterConfig getConfig() {
        return this.config;
    }

    public static class RepeaterConfig {
        /**
         * delay in ticks
         */
        public int delay = 0;

        /**
         * power loss during "transfer"
         */
        public int loss = 0;
    }
}