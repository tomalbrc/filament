package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.block.BlockBehaviour;
import net.minecraft.resources.ResourceLocation;

/**
 * Block behaviourConfig for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Strippable implements BlockBehaviour<Strippable.StrippableConfig> {
    private final StrippableConfig config;

    public Strippable(StrippableConfig config) {
        this.config = config;
    }

    @Override
    public StrippableConfig getConfig() {
        return this.config;
    }

    public static class StrippableConfig {
        /**
         * Replacement block
         */
        public ResourceLocation replacement;
    }
}