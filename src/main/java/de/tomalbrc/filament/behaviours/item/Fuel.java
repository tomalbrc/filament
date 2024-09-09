package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviours.BehaviourHolder;
import de.tomalbrc.filament.registry.FuelRegistry;
import net.minecraft.world.item.Item;

/**
 * Fuel behaviour
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

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        FuelRegistry.add(item, config.value);
    }

    public static class FuelConfig {
        /**
         * The value associated with the fuel, used in furnaces and similar item burning blocks
         */
        public int value = 10;
    }
}