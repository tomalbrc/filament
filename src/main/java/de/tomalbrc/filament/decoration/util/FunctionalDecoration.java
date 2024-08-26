package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.behaviours.decoration.*;
import org.jetbrains.annotations.NotNull;

public interface FunctionalDecoration {
    void setupBehaviour(DecorationData decorationData);

    void setAnimationData(@NotNull Animation animationData);

    void setSeatData(@NotNull Seat seatData);

    void setShowcaseData(@NotNull Showcase showcaseData);

    void setLockData(Lock lockData);

    void setContainerData(@NotNull Container containerData);
}
