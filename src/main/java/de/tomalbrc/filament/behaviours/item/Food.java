package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import org.jetbrains.annotations.NotNull;

/**
 * Food behaviours for edible items
 */
public class Food implements ItemBehaviour<Food.FoodConfig> {
    private final FoodConfig config;

    public Food(FoodConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public FoodConfig getConfig() {
        return this.config;
    }

    public static class FoodConfig {
        /**
         * The amount of hunger restored when consumed.
         */
        public int hunger = 1;

        /**
         * The saturation modifier provided by the food.
         */
        public float saturation = 0.6f;

        /**
         * Indicates whether the item can be eaten when the hunger bar is full
         */
        public boolean canAlwaysEat = false;

        /**
         * Fast food, 0.8 secs, default is 1.6f
         */
        public boolean fastfood = false;
    }
}
