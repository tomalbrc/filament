package de.tomalbrc.filament.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.block.*;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.*;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BlockRegistry {
    public static Map<ResourceLocation, Collection<ResourceLocation>> BLOCKS_TAGS = new Object2ReferenceOpenHashMap<>();

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
    public static void register(BlockData data) {
        if (BuiltInRegistries.BLOCK.containsKey(data.id())) return;

        BlockProperties properties = data.properties();
        BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

        SimpleBlock customBlock;
        // ok this is starting to get messy
        if (data.requiresEntityBlock()) {
            if (data.virtual()) {
                customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleVirtualBlockWithEntity(props, data), blockProperties, data.blockTags());
            } else {
                customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleBlockWithEntity(props, data), blockProperties, data.blockTags());
            }
        } else {
            if (data.virtual()) {
                customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleVirtualBlock(props, data), blockProperties, data.blockTags());
            } else {
                customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleBlock(props, data), blockProperties, data.blockTags());
            }
        }

        Item.Properties itemProperties = data.properties().toItemProperties();
        if (data.properties().copyComponents) {
            for (TypedDataComponent component : data.vanillaItem().components()) {
                itemProperties.component(component.type(), component.value());
            }
        }
        for (TypedDataComponent component : data.components()) {
            itemProperties.component(component.type(), component.value());
        }

        SimpleBlockItem item = ItemRegistry.registerItem(ItemRegistry.key(data.id()), (newProps) -> new SimpleBlockItem(newProps, customBlock, data), itemProperties, data.group() != null ? data.group() : Constants.BLOCK_GROUP_ID, data.itemTags());
        BehaviourUtil.postInitItem(item, item, data.behaviour());
        BehaviourUtil.postInitBlock(item, customBlock, customBlock, data.behaviour());
        Translations.add(item, customBlock, data);

        customBlock.postRegister();

        RPUtil.create(item, data);

        FilamentRegistrationEvents.BLOCK.invoker().registered(data, item, customBlock);
    }

    public static ResourceKey<Block> key(ResourceLocation id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    public static <T extends Block> T registerBlock(ResourceKey<Block> resourceKey, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, @Nullable Set<ResourceLocation> blockTags) {
        T block = function.apply(properties.setId(resourceKey));
        BLOCKS_TAGS.put(resourceKey.location(), blockTags);
        return Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);
    }

    public static class BlockDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID);
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/block", null, resourceManager, (id, inputStream) -> {
                try {
                    BlockRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load block resource \"{}\".", id);
                }
            });
        }
    }
}
