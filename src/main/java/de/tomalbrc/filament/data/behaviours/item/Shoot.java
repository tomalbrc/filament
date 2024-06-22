package de.tomalbrc.filament.data.behaviours.item;

import net.minecraft.resources.ResourceLocation;

/**
 * Item behaviours for projectile shooting
 */
public class Shoot {
    /**
     * Indicates whether the shooting action consumes the item
     */
    public boolean consumes;

    /**
     * The base damage of the projectile.
     */
    public double baseDamage = 2.0;

    /**
     * The speed at which the projectile is fired.
     */
    public double speed = 1.0;

    /**
     * The identifier for the projectile item
     */
    public ResourceLocation projectile;

    /**
     * Sound effect to play when shooting
     */
    public ResourceLocation sound;
}
