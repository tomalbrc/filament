package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;

/**
 * Fuel behaviourConfig
 */
public class Fuel implements ItemBehaviour<Fuel.FuelConfig> {
    private final FuelConfig config;

    public Fuel(FuelConfig config) {
        this.config = config;
    }

    @Override
    public FuelConfig getConfig() {
        return this.config;
    }

    public static class FuelConfig {
        /**
         * The value associated with the fuel, used in furnaces and similar item burning blocks
         */
        public int value = 10;
    }
}