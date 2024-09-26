package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Banner pattern behaviour
 */
public class BannerPattern implements ItemBehaviour<BannerPattern.Config> {
    private final Config config;

    public BannerPattern(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public BannerPattern.Config getConfig() {
        return this.config;
    }

    public static class Config {
        /**
         * The id of the banner pattern
         */
        public ResourceLocation id;
    }
}