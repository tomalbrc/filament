package de.tomalbrc.filament.data.behaviours.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic {
    /**
     * The equipment slot for the cosmetic (head, chest).
     */
    public EquipmentSlot slot;

    /**
     * The resource location of the model for the cosmetic.
     */
    public ResourceLocation model;

    /**
     * The name of the location to autoplay. The animation should be loopable
     */
    public String animation;
}
