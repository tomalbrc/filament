package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.registry.BlockTypeRegistry;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.behaviours.block.Strippable;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
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

        Class<? extends Block> blockClass = BlockTypeRegistry.get(data.type());
        Block customBlock;
        try {
            customBlock = blockClass.getDeclaredConstructor(BlockBehaviour.Properties.class, BlockData.class).newInstance(blockProperties, data);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (customBlock != null) {
            var itemProperties = data.properties().toItemProperties();
            if (data.components() != null) {
                for (TypedDataComponent component : data.components()) {
                    itemProperties.component(component.type(), component.value());
                }
            }

            if (data.isStrippable()) {
                Strippable strippable = data.behaviour().get(Constants.Behaviours.STRIPPABLE);
                StrippableRegistry.add(customBlock.defaultBlockState(), strippable.replacement);
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
