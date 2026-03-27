package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(FolderRepositorySource.class)
public interface FolderRepositorySourceAccessor {
    @Accessor
    Path getFolder();
}
