package de.tomalbrc.filament.data.behaviours.item;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Mob trap
 */
public class Trap {
    // allowed util types to trap
    public List<ResourceLocation> types = null;

    public List<ResourceLocation> requiredEffects = null;

    public int chance = 50;

    public int useDuration = 0;
}
