package de.tomalbrc.filament.data.behaviours.item;

import de.tomalbrc.filament.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Armor item behaviours, using fancypants shader via polymer
 */
public class Armor implements ItemBehaviour {
    /**
     * The equipment slot for the armor piece (e.g., head, chest, legs, or feet).
     */
    public EquipmentSlot slot;

    /**
     * The resource location of the texture associated with the armor.
     */
    public ResourceLocation texture;
}
