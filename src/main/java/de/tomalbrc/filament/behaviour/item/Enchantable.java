package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Enchantable behaviour
 */
public class Enchantable implements ItemBehaviour<Enchantable.Config> {
    private final Config config;

    public Enchantable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Enchantable.Config getConfig() {
        return this.config;
    }

    @Override
    @NotNull
    public Optional<Integer> getEnchantmentValue() {
        return Optional.of(config.value);
    }

    public static class Config {
        public int value = 1;
    }
}