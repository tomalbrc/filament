package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.WaxableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for waxing blocks (like copper)
 * Copies blockstate properties if applicable
 */
public class Waxable implements BlockBehaviour<Waxable.Config> {
    private final Config config;

    public Waxable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Waxable.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        WaxableRegistry.add(block, config.replacement);
    }

    public static class Config {
        /**
         * Replacement block
         */
        public ResourceLocation replacement;
    }
}