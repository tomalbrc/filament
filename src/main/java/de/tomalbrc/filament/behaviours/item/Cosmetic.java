package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic implements ItemBehaviour<Cosmetic.CosmeticConfig> {
    private final CosmeticConfig config;

    public Cosmetic(CosmeticConfig config) {
        this.config = config;
    }

    @Override
    public CosmeticConfig getConfig() {
        return this.config;
    }

    public static class CosmeticConfig {
        /**
         * The equipment slot for the cosmetic (head, chest).
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
