package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for flammable blocks and decorations. (Uses filament registration events + fabric flammable registry)
 */
public class Flammable implements BlockBehaviour<Flammable.Config> {
    private final Config config;

    public Flammable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Flammable.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        FlammableBlockRegistry.getDefaultInstance().add(block, config.burn, config.spread);
    }

    public static class Config {
        public int spread = 20;
        public int burn = 5;
    }
}