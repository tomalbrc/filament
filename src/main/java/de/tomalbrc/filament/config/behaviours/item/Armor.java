package de.tomalbrc.filament.config.behaviours.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Armor item behaviours, using fancypants shader via polymer
 */
public class Armor {
    /**
     * The equipment slot for the armor piece (e.g., head, chest, legs, or feet).
     */
    public EquipmentSlot slot;

    /**
     * The resource location of the texture associated with the armor.
     */
    public ResourceLocation texture;
}
