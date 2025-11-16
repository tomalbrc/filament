package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.StrippableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for strippable blocks (with an axe)
 * Copies blockstate properties if applicable
 */
public class Strippable implements BlockBehaviour<Strippable.Config> {
    private final Config config;

    public Strippable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Strippable.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        StrippableRegistry.add(block, config.replacement, config.lootTable);
    }

    public static class Config {
        /**
         * Replacement block
         */
        public ResourceLocation replacement;
        /**
         * Loot to drop when a block is stripped
         */
        public ResourceLocation lootTable;

        /**
         * Copper-like scrape particles
         */
        public boolean scrape = false;

        /**
         * Wax-scrape particles
         */
        public boolean scrapeWax = false;

        /**
         * Sound to play
         */
        public ResourceLocation sound = SoundEvents.AXE_STRIP.getLocation();
    }
}