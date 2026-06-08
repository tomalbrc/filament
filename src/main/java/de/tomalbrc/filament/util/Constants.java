package de.tomalbrc.filament.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

import java.nio.file.Path;

public class Constants {
    public static final String MOD_ID = "filament";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    public static final Identifier ITEM_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item");
    public static final Identifier BLOCK_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block");
    public static final Identifier DECORATION_GROUP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "decoration");

    public static final BlockState ERROR_BLOCK_STATE = Blocks.TEST_BLOCK.defaultBlockState().setValue(BlockStateProperties.TEST_BLOCK_MODE, TestBlockMode.FAIL);
}
