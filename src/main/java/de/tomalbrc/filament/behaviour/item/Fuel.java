package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.FuelRegistry;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Fuel behaviour
 */
public class Fuel implements ItemBehaviour<Fuel.Config> {
    private final Config config;

    public Fuel(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Fuel.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        FuelRegistry.add(item, config.value);
    }

    public static class Config {
        /**
         * The value associated with the fuel, used in furnaces and similar item burning blocks
         */
        public int value = 10;
    }
}