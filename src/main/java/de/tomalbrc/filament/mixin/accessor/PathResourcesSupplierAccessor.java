package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.server.packs.PathPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(PathPackResources.PathResourcesSupplier.class)
public interface PathResourcesSupplierAccessor {
    @Accessor
    Path getContent();
}
