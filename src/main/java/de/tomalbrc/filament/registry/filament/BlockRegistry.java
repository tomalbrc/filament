package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.block.*;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Json;
import eu.pb4.polymer.blocks.api.BlockModelType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BlockRegistry {
    public static int REGISTERED_BLOCKS = 0;

    public static void register(InputStream inputStream) throws IOException {
        register(Json.GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), BlockData.class));
    }

    public static void register(BlockData data) throws IOException {
        BlockProperties properties = data.properties();
        if (BuiltInRegistries.BLOCK.containsKey(data.id())) return;

        BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

        Block customBlock = switch (data.type() == null ? BlockData.BlockType.block : data.type()) {
            case block -> new SimpleBlock(blockProperties, data);
            case column -> new AxisBlock(blockProperties, data);
            case count -> new CountBlock(blockProperties, data);
            case powerlevel -> new PowerlevelBlock(blockProperties, data);
            case powered_directional -> new PoweredDirectionBlock(blockProperties, data);
            case slab -> new SimpleSlabBlock(blockProperties, data);
            case directional, horizontal_directional -> throw new UnsupportedOperationException("Not implemented");
        };

        if (customBlock != null) {
            var itemProperties = data.properties().toItemProperties();
            if (data.components() != null) {
                for (TypedDataComponent component : data.components()) {
                    itemProperties.component(component.type(), component.value());
                }
            }

            SimpleBlockItem item = new SimpleBlockItem(itemProperties, customBlock, data);
            BlockRegistry.registerBlock(data.id(), customBlock);
            ItemRegistry.registerItem(data.id(), item, ItemRegistry.CUSTOM_BLOCK_ITEMS);

            REGISTERED_BLOCKS++;
        } else {
            throw new IOException(String.format("Could not read block type {}", data.type()));
        }
    }

    public static void registerBlock(ResourceLocation identifier, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    public static class BlockDataReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath("filament", "filament");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/block", path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {

                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    BlockData data = Json.GSON.fromJson(reader, BlockData.class);
                    BlockRegistry.register(data);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load block resource \"" + entry.getKey() + "\".");
                }
            }

            Filament.LOGGER.info("filament blocks registered: " + BlockRegistry.REGISTERED_BLOCKS);
        }
    }
}
