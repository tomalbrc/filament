package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.StrippableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    public StrippableConfig getConfig() {
        return this.config;
    }

    @Override
    public void init(Block block, BehaviourHolder behaviourHolder) {
        StrippableRegistry.add(block, config.replacement);
    }

    public static class StrippableConfig {
        /**
         * Replacement block
         */
        public ResourceLocation replacement;
    }
}