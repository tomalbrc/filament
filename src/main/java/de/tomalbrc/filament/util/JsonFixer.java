package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.data.Data;

public interface JsonFixer<T extends Data<?>> {
    void apply(T data, JsonElement element);
}
