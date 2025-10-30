package de.tomalbrc.filament.data.entity;

import net.minecraft.resources.ResourceLocation;

public record EntityAnimationInfo(
        ResourceLocation model,
        String idleAnimation,
        String walkAnimation
) {
}
