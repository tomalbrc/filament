package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.block.*;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Objects;

public class BlockRegistry {
    public static int REGISTERED_BLOCKS = 0;

    public static final File DIR = Constants.CONFIG_DIR.resolve("block").toFile();

    public static void register() {
        if (!DIR.exists() || !DIR.isDirectory()) {
            DIR.mkdirs();
            return;
        }

        Collection<File> files = FileUtils.listFiles(DIR, new String[]{"json"}, true);
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    BlockData data = Json.GSON.fromJson(reader, BlockData.class);
                    BlockProperties properties = data.properties();
                    BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

                    Block customBlock = switch (data.type() == null ? BlockData.BlockType.block : data.type()) {
                        case block -> new SimpleBlock(blockProperties, data);
                        case column -> new AxisBlock(blockProperties, data);
                        case count -> new CountBlock(blockProperties, data);
                        case powerlevel -> new PowerlevelBlock(blockProperties, data);
                        case powered_directional -> new PoweredDirectionBlock(blockProperties, data);
                        case directional, horizontal_directional -> throw new UnsupportedOperationException("Not implemented");
                    };

                    if (customBlock != null) {
                        SimpleBlockItem item = new SimpleBlockItem(new Item.Properties(), customBlock, data);
                        BlockRegistry.registerBlock(data.id(), customBlock);
                        ItemRegistry.registerItem(data.id(), item, ItemRegistry.CUSTOM_BLOCK_ITEMS);

                        REGISTERED_BLOCKS++;
                    } else {
                        Filament.LOGGER.error("Could not read block type {} from {}", data.type(), file.getAbsolutePath());
                    }
                } catch (Throwable throwable) {
                    Filament.LOGGER.error("Error reading block JSON file: {}", file.getAbsolutePath(), throwable);
                }
            }
        }
    }

    public static void registerBlock(ResourceLocation identifier, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }
}
