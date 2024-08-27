package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.block.BlockBehaviour;

/**
 * Block behaviours for redstone power source
 */
public class Powersource implements BlockBehaviour<Powersource.PowersourceConfig> {
    private final PowersourceConfig config;

    public Powersource(PowersourceConfig config) {
        this.config = config;
    }

    @Override
    public PowersourceConfig getConfig() {
        return this.config;
    }

    public static class PowersourceConfig {
        /**
         * The redstone power value
         */
        public int value = 15;
    }
}
