package de.tomalbrc.filament.util;

import de.tomalbrc.filament.data.BlockData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class Constants {
    public static final String MOD_ID = "filament";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();


    public static class Behaviours {
        public static final ResourceLocation COMPOSTABLE = ResourceLocation.fromNamespaceAndPath("filament", "compostable");
        public static final ResourceLocation FUEL = ResourceLocation.fromNamespaceAndPath("filament", "fuel");

        // Blocks
        public static final ResourceLocation POWERSOURCE = ResourceLocation.fromNamespaceAndPath("filament", "powersource");
        public static final ResourceLocation REPEATER = ResourceLocation.fromNamespaceAndPath("filament", "repeater");
        public static final ResourceLocation STRIPPABLE = ResourceLocation.fromNamespaceAndPath("filament", "strippable");
        public static final ResourceLocation COLUMN = ResourceLocation.fromNamespaceAndPath("filament", "column");
        public static final ResourceLocation COUNT = ResourceLocation.fromNamespaceAndPath("filament", "count");
        public static final ResourceLocation DIRECTIONAL_POWERED = ResourceLocation.fromNamespaceAndPath("filament", "directional_powered");
        public static final ResourceLocation DIRECTIONAL = ResourceLocation.fromNamespaceAndPath("filament", "directional");
        public static final ResourceLocation POWERLEVEL = ResourceLocation.fromNamespaceAndPath("filament", "powerlevel");
        public static final ResourceLocation SLAB = ResourceLocation.fromNamespaceAndPath("filament", "slab");

        // Items
        public static final ResourceLocation COSMETIC = ResourceLocation.fromNamespaceAndPath("filament", "cosmetic");
        public static final ResourceLocation FOOD = ResourceLocation.fromNamespaceAndPath("filament", "food");
        public static final ResourceLocation ARMOR = ResourceLocation.fromNamespaceAndPath("filament", "armor");

        public static final ResourceLocation SHOOT = ResourceLocation.fromNamespaceAndPath("filament", "shoot");
        public static final ResourceLocation INSTRUMENT = ResourceLocation.fromNamespaceAndPath("filament", "instrument");
        public static final ResourceLocation TRAP = ResourceLocation.fromNamespaceAndPath("filament", "trap");
        public static final ResourceLocation EXECUTE = ResourceLocation.fromNamespaceAndPath("filament", "execute");

        // decoration
        public static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("filament", "animation");
        public static final ResourceLocation CONTAINER = ResourceLocation.fromNamespaceAndPath("filament", "container");
        public static final ResourceLocation LOCK = ResourceLocation.fromNamespaceAndPath("filament", "lock");
        public static final ResourceLocation SEAT = ResourceLocation.fromNamespaceAndPath("filament", "seat");
        public static final ResourceLocation SHOWCASE = ResourceLocation.fromNamespaceAndPath("filament", "showcase");
    }

    public static class BlockTypes {
        public static final BlockData.BlockType BLOCK = BlockData.BlockType.ofFilament("block");
        public static final BlockData.BlockType COUNT = BlockData.BlockType.ofFilament("count");
        public static final BlockData.BlockType COLUMN = BlockData.BlockType.ofFilament("column");
        public static final BlockData.BlockType DIRECTIONAL = BlockData.BlockType.ofFilament("directional");
        public static final BlockData.BlockType HORIZONTAL_DIRECTIONAL = BlockData.BlockType.ofFilament("horizontal_directional");
        public static final BlockData.BlockType POWERED_DIRECTIONAL = BlockData.BlockType.ofFilament("powered_directional");
        public static final BlockData.BlockType POWERLEVEL = BlockData.BlockType.ofFilament("powerlevel");
        public static final BlockData.BlockType SLAB = BlockData.BlockType.ofFilament("slab");
    }
}
