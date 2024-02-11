package de.tomalbrc.filament.data.misc;

import net.minecraft.resources.ResourceLocation;

// Big TODO, for armor etc

@SuppressWarnings("unused")
public record Effect(
        ResourceLocation effect,
                     int duration,
                     int level,
                     int delay
) {}
