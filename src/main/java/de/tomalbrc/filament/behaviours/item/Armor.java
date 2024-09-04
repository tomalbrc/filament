package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Armor item behaviours, using fancypants shader via polymer
 */
public class Armor implements ItemBehaviour<Armor.ArmorConfig> {
    private final ArmorConfig config;

    public Armor(ArmorConfig config) {
        this.config = config;
    }

    @Override
    public ArmorConfig getConfig() {
        return this.config;
    }

    public static class ArmorConfig {
        /**
         * The equipment slot for the armor piece (e.g., head, chest, legs, or feet).
         */
        public EquipmentSlot slot;

        /**
         * The resource location of the texture associated with the armor.
         */
        public ResourceLocation texture;

        /**
         * Flag whether to use armor trims instead of shader based armor
         */
        public boolean trim = false;
    }
}
