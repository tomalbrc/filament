package de.tomalbrc.filament.decoration.util;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.behaviours.decoration.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FunctionalDecoration {
    void setupBehaviour(DecorationData decorationData);

    void setAnimationData(@NotNull Animation animationData);

    void setSeatData(@NotNull List<Seat> seatData);

    void setShowcaseData(@NotNull List<Showcase> showcaseData);

    void setLockData(Lock lockData);

    void setContainerData(@NotNull Container containerData);
}
