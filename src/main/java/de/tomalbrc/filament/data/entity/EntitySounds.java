package de.tomalbrc.filament.data.entity;

import net.minecraft.resources.ResourceLocation;

public record EntitySounds(ResourceLocation ambient, ResourceLocation swim, ResourceLocation swimSplash,
                           ResourceLocation hurt, ResourceLocation death, FallSounds fall) {

}
