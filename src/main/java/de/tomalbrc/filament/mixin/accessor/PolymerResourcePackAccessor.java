package de.tomalbrc.filament.mixin.accessor;

import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(value = PolymerResourcePack.class, remap = false)
public interface PolymerResourcePackAccessor {
    @Accessor
    static void setPath(Path path) {
        throw new UnsupportedOperationException();
    }
}
