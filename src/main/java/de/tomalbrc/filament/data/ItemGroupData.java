package de.tomalbrc.filament.data;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public record ItemGroupData(
        @NotNull ResourceLocation id,
        @NotNull ResourceLocation item,
        @Nullable String literal
) {

}
