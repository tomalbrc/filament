package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Compostable behaviour
 */
public class Compostable implements ItemBehaviour<Compostable.CompostableConfig> {
    private final CompostableConfig config;

    public Compostable(CompostableConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public CompostableConfig getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        CompostingChanceRegistry.INSTANCE.add(item, this.config.chance/100.f);
        if (config.villagerInteraction) VillagerInteractionRegistries.registerCompostable(item);
    }

    public static class CompostableConfig {
        /**
         * Chance of successful compostation in percent, from 0 to 100
         */
        public float chance = 50;

        /**
         * Flag whether villagers can compost the item
         */
        public boolean villagerInteraction = true;
    }
}