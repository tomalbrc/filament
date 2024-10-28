package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {
    public static int REGISTERED_BLOCKS = 0;

    private static List<Runnable> LATE_ITEMS = new ObjectArrayList<>();

    public static void addLate() {
        LATE_ITEMS.forEach(Runnable::run);
        LATE_ITEMS.clear();
    }

    public static void register(InputStream inputStream) throws IOException {
        var bytes = inputStream.readAllBytes();
        register(Json.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), BlockData.class), bytes);
    }

    public static void register(BlockData data, byte[] reader) throws IOException {
        if (BuiltInRegistries.BLOCK.containsKey(data.id())) return;

        BlockProperties properties = data.properties();
        BlockBehaviour.Properties blockProperties = properties.toBlockProperties();

        SimpleBlock customBlock = BlockRegistry.registerBlock(key(data.id()), (props)-> new SimpleBlock(props, data), blockProperties);

        Item.Properties itemProperties = data.properties().toItemProperties();
        for (TypedDataComponent component : data.components()) {
            itemProperties.component(component.type(), component.value());
        }


        SimpleBlockItem item = ItemRegistry.registerItem(ItemRegistry.key(data.id()), (newProps) -> new SimpleBlockItem(newProps, customBlock, data), itemProperties, data.itemGroup() != null ? data.itemGroup() : Constants.BLOCK_GROUP_ID);
        BehaviourUtil.postInitItem(item, item, data.behaviourConfig());
        BehaviourUtil.postInitBlock(item, customBlock, customBlock, data.behaviourConfig());

        customBlock.postRegister();

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
                    var bytes = input.readAllBytes();
                    BlockData data = Json.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), BlockData.class);
                    BlockRegistry.register(data, bytes);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load block resource \"{}\".", entry.getKey());
                }
            }
        }
    }
}
