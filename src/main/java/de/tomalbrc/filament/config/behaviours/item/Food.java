package de.tomalbrc.filament.config.behaviours.item;

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
     * Indicates whether the food is classified as meat.
     */
    public boolean meat = false;
}
