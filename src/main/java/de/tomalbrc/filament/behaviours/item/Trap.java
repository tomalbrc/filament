package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Mob trap
 */
public class Trap implements ItemBehaviour<Trap.TrapConfig> {
    private final TrapConfig config;

    public Trap(TrapConfig config) {
        this.config = config;
    }

    @Override
    public TrapConfig getConfig() {
        return config;
    }

    public static class TrapConfig {
        // allowed util types to trap
        public List<ResourceLocation> types = null;

        public List<ResourceLocation> requiredEffects = null;

        public int chance = 50;

        public int useDuration = 0;
    }
}
