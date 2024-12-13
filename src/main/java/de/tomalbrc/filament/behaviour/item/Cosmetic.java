package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic implements ItemBehaviour<Cosmetic.Config> {
    private final Config config;

    public Cosmetic(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Cosmetic.Config getConfig() {
        return this.config;
    }

    public static class Config {
        /**
         * The equipment slot for the cosmetic
         */
        public EquipmentSlot slot;

        /**
         * The resource location of the animated model for the cosmetic.
         */
        public ResourceLocation model;

        /**
         * The name of the animation to autoplay. The animation should be loopable
         */
        public String autoplay;

        /**
         * Scale of the chest cosmetic
         */
        public Vector3f scale = new Vector3f(1);

        /**
         * Translation of the chest cosmetic
         */
        public Vector3f translation = new Vector3f();
    }
}
