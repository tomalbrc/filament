package de.tomalbrc.filament.data.behaviours.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic {
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
