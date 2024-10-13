package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Food behaviour for edible items
 */
public class VillagerFood implements ItemBehaviour<VillagerFood.Config> {
    private final Config config;

    public VillagerFood(Config config) {
        this.config = config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        VillagerInteractionRegistries.registerFood(item, this.config.value);
    }

    @Override
    @NotNull
    public VillagerFood.Config getConfig() {
        return this.config;
    }

    public static class Config {
        /**
         * The amount of "breeding power" the item has (1 = normal food item, 4 = bread). Defaults to 1
         */
        public int value = 1;
    }
}
