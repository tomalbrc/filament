package de.tomalbrc.filament.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class Constants {
    public static final String MOD_ID = "filament";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    public static final ResourceLocation ITEM_GROUP_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item");
    public static final ResourceLocation BLOCK_GROUP_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block");
    public static final ResourceLocation DECORATION_GROUP_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "decoration");
}
