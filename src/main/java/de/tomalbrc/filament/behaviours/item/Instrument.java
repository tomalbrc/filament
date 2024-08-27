package de.tomalbrc.filament.behaviours.item;

import de.tomalbrc.filament.api.behaviour.item.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;

public class Instrument implements ItemBehaviour<Instrument.InstrumentConfig> {
    private final InstrumentConfig config;

    public Instrument(InstrumentConfig config) {
        this.config = config;
    }

    @Override
    public InstrumentConfig getConfig() {
        return this.config;
    }

    public static class InstrumentConfig {
        public ResourceLocation sound = null;

        public int range = 0;

        public int useDuration = 0;
    }
}
