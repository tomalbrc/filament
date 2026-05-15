package de.tomalbrc.filamentweb.asset;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

public interface Resource<RawDataType> {
    UUID getId();

    RawDataType raw();

    @Nullable Path path();
    @NotNull String icon();

    String displayName();

    boolean isReadOnly();

    void setDirty(boolean dirty);
    boolean isDirty();

    boolean writeFile();

    Identifier id();
}
