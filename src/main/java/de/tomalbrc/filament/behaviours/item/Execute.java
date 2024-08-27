package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

public class Execute implements ItemBehaviour<Execute.ExecuteConfig> {
    private final ExecuteConfig config;

    public Execute(ExecuteConfig config) {
        this.config = config;
    }

    @Override
    public ExecuteConfig getConfig() {
        return null;
    }

    public static class ExecuteConfig {
        public boolean consumes;

        public String command;

        public ResourceLocation sound;
    }
}
