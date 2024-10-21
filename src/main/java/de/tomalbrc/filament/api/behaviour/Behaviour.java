package de.tomalbrc.filament.api.behaviour;

import org.jetbrains.annotations.NotNull;

public interface Behaviour<T> {
    @NotNull
    T getConfig();
}
