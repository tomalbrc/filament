package de.tomalbrc.filament.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.Translations;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;

public class BlockRegistry {
    public static int REGISTERED_BLOCKS = 0;

    public static void register(InputStream inputStream) throws IOException {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(inputStream));
        try {
            BlockData data = Json.GSON.fromJson(element, BlockData.class);

            Util.handleComponentsCustom(element, data);

            register(data);
        } catch (Exception e) {
            Filament.LOGGER.error("Could not load file! Error: {}", String.valueOf(e.fillInStackTrace()));
            Filament.LOGGER.info(element.toString());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(BlockData data) throws IOException {
        if (BuiltInRegistries.BLOCK.containsKey(data.id())) return;

        BlockProperties properties = data.properties();
        BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

        SimpleBlock customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleBlock(props, data), blockProperties);

        Item.Properties itemProperties = data.properties().toItemProperties();
        for (TypedDataComponent component : data.components()) {
            itemProperties.component(component.type(), component.value());
        }

        SimpleBlockItem item = ItemRegistry.registerItem(ItemRegistry.key(data.id()), (newProps) -> new SimpleBlockItem(newProps, customBlock, data), itemProperties, data.group() != null ? data.group() : Constants.BLOCK_GROUP_ID);
        BehaviourUtil.postInitItem(item, item, data.behaviour());
        BehaviourUtil.postInitBlock(item, customBlock, customBlock, data.behaviour());
        Translations.add(item, customBlock, data);

        customBlock.postRegister();

        var itemResources = data.itemResource() == null ? data.blockResource() : data.itemResource();
        if (itemResources != null && data.itemModel() == null && itemResources.getModels() != null) {
            PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder ->
                    ItemAssetGenerator.create(resourcePackBuilder, data.id(), itemResources, data.vanillaItem().components().has(DataComponents.DYED_COLOR))
            );
        }

        REGISTERED_BLOCKS++;
    }

    public static ResourceKey<Block> key(ResourceLocation id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    public static <T extends Block> T registerBlock(ResourceKey<Block> resourceKey, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
        T block = function.apply(properties.setId(resourceKey));
        return Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);
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
                try (var input = entry.getValue().open()) {
                    BlockRegistry.register(input);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load block resource \"{}\".", entry.getKey());
                }
            }
        }
    }
}
