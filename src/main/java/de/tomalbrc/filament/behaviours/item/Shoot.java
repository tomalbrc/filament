package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

/**
 * Item behaviours for projectile shooting
 */
public class Shoot implements ItemBehaviour<Shoot.ShootConfig> {
    private final ShootConfig config;

    public Shoot(ShootConfig config) {
        this.config = config;
    }

    @Override
    public ShootConfig getConfig() {
        return this.config;
    }

    public static class ShootConfig {
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
}
