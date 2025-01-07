package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Translations;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BlockRegistry {
    public static int REGISTERED_BLOCKS = 0;

    public static void register(InputStream inputStream) throws IOException {
        register(Json.GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), BlockData.class));
    }

    public static void register(BlockData data) throws IOException {
        if (BuiltInRegistries.BLOCK.containsKey(data.id())) return;

        BlockProperties properties = data.properties();
        BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

        SimpleBlock customBlock = new SimpleBlock(blockProperties, data);

        Item.Properties itemProperties = data.properties().toItemProperties();
        if (data.properties().copyComponents) {
            for (TypedDataComponent component : data.vanillaItem().components()) {
                itemProperties.component(component.type(), component.value());
            }
        }

        for (TypedDataComponent component : data.components()) {
            itemProperties.component(component.type(), component.value());
        }


        SimpleBlockItem item = new SimpleBlockItem(itemProperties, customBlock, data);
        BehaviourUtil.postInitItem(item, item, data.behaviourConfig());
        BehaviourUtil.postInitBlock(item, customBlock, customBlock, data.behaviourConfig());
        Translations.add(item, customBlock, data);

        BlockRegistry.registerBlock(data.id(), customBlock);
        ItemRegistry.registerItem(data.id(), item, data.itemGroup() != null ? data.itemGroup() : Constants.BLOCK_GROUP_ID);

        customBlock.postRegister();

        REGISTERED_BLOCKS++;
    }

    public static void registerBlock(ResourceLocation identifier, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    public static class BlockDataReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID);
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/block", path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {

                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    BlockData data = Json.GSON.fromJson(reader, BlockData.class);
                    BlockRegistry.register(data);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load block resource \"{}\".", entry.getKey());
                }
            }
        }
    }
}
