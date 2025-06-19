package de.tomalbrc.filament.datafixer.config;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;

public class BlockDataFix {
    public static void fixup(JsonElement element, AbstractBlockData<? extends BlockProperties> data) {
        var props = element.getAsJsonObject().get("virtual");
        if (props != null && props.getAsBoolean()) {
            data.properties().virtual = true;
        }
    }
}
