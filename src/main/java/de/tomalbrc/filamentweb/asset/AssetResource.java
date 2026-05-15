package de.tomalbrc.filamentweb.asset;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

public class AssetResource implements Resource<byte[]> {
    final UUID id;
    final Path path;
    final String assetPath;
    byte[] raw;

    boolean dirty;

    public AssetResource(UUID id, Path file, String assetPath) {
        this.id = id;
        this.path = file;
        this.assetPath = assetPath;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public byte[] raw() {
        return raw;
    }

    @Override
    public @Nullable Path path() {
        return path;
    }

    @Override
    public @NotNull String icon() {
        return "A";
    }

    @Override
    public String displayName() {
        return assetPath;
    }

    @Override
    public boolean isReadOnly() {
        return path == null;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean writeFile() {
        return false;
    }

    @Override
    public Identifier id() {
        return null;
    }
}
