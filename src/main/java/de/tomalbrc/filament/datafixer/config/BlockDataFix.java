package de.tomalbrc.filament.datafixer.config;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.JsonFixer;

public class BlockDataFix implements JsonFixer<AbstractBlockData<? extends BlockProperties>> {
    @Override
    public void apply(AbstractBlockData<? extends BlockProperties> data, JsonElement element) {
        var props = element.getAsJsonObject().get("virtual");
        if (props != null && props.getAsBoolean()) {
            data.properties().virtual = true;
        }
    }
}
