package de.tomalbrc.filament.injection;

import de.tomalbrc.filament.decoration.DecorationPreviewManager;
import org.jetbrains.annotations.Nullable;

public interface DecorationPreviewHolder {
    @Nullable
    DecorationPreviewManager.PreviewState filament$getPreviewState();

    void filament$setPreviewState(@Nullable DecorationPreviewManager.PreviewState state);
}