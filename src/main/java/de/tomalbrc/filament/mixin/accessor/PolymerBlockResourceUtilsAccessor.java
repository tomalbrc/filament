package de.tomalbrc.filament.mixin.accessor;

import eu.pb4.polymer.blocks.api.BlockResourceCreator;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PolymerBlockResourceUtils.class)
public interface PolymerBlockResourceUtilsAccessor {
    @Accessor
    static BlockResourceCreator getCREATOR() {
        throw new AssertionError();
    }
}
