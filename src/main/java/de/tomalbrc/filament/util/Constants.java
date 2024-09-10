package de.tomalbrc.filament.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class Constants {
    public static final String MOD_ID = "filament";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
}
