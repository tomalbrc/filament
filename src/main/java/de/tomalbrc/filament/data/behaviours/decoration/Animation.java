package de.tomalbrc.filament.data.behaviours.decoration;

import net.minecraft.resources.ResourceLocation;

/**
 * Animation behaviours for decoration using animated java models (powered by nylon)
 */
public class Animation {
    /**
     * The name of the animated model associated with this animation (if applicable).
     */
    public ResourceLocation model = null;

    /**
     * The name of the animation to autoplay (if specified)
     */
    public String autoplay = null;
}