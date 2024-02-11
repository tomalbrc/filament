package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.block.*;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;

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

                    Block block;
                    if (data.hasState("axis")) {
                        block = new AxisBlock(blockProperties, data);
                    } else if (data.hasState("count")) {
                        block = new CountBlock(blockProperties, data);
                    } else if (data.hasState("powerlevel")) {
                        block = new PowerlevelBlock(blockProperties, data);
                    } else if (data.hasState("powereddirection")) {
                        block = new PoweredDirectionBlock(blockProperties, data);
                    } else {
                        block = new SimpleBlock(blockProperties, data);
                    }

                    SimpleBlockItem item = new SimpleBlockItem(new Item.Properties(), block, data);
                    BlockRegistry.registerBlock(data.id(), block);
                    ItemRegistry.registerItem(data.id(), item, ItemRegistry.CUSTOM_BLOCK_ITEMS);

                    REGISTERED_BLOCKS++;
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
