package de.tomalbrc.filament.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public record ItemGroupData(
        @NotNull Identifier id,
        @NotNull Identifier item,
        @Nullable Component literal
) {

}
