package de.tomalbrc.filament.data.behaviours.item;

import net.minecraft.world.food.FoodProperties;

/**
 * Food behaviours for edible items
 */
public class Food {
    /**
     * The amount of hunger restored when consumed.
     */
    public int hunger = 1;

    /**
     * The saturation modifier provided by the food.
     */
    public float saturation = 0.6f;

    /**
     * Indicates wether the item can be eaten when the hunger bar is full
     */
    public boolean canAlwaysEat = false;

    /**
     * Fast food, 0.8 secs, default is 1.6f
     */
    public boolean fastfood = false;
}
