package de.tomalbrc.filament.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

public class Constants {
    public static final String MOD_ID = "filament";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    public static final Identifier ITEM_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item");
    public static final Identifier BLOCK_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block");
    public static final Identifier DECORATION_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "decoration");
}
