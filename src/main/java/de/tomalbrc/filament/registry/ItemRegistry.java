package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static int REGISTERED_ITEMS = 0;

    public static void register(InputStream inputStream) throws IOException {
        register(Json.GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), ItemData.class));
    }

    static public void register(ItemData data) {
        if (BuiltInRegistries.ITEM.containsKey(data.id())) return;

        Item.Properties properties = data.properties().toItemProperties(data.behaviourConfig());

        for (TypedDataComponent component : data.components()) {
            properties.component(component.type(), component.value());
        }


        var item = ItemRegistry.registerItem(key(data.id()), (newProps) -> new SimpleItem(null, newProps, data, data.vanillaItem()), properties, data.itemGroup() != null ? data.itemGroup() : Constants.ITEM_GROUP_ID);
        BehaviourUtil.postInitItem(item, item, data.behaviourConfig());

        REGISTERED_ITEMS++;
    }

    public static ResourceKey<Item> key(ResourceLocation id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    public static <T extends Item> T registerItem(ResourceKey<Item> identifier, Function<Item.Properties, T> function, Item.Properties properties, ResourceLocation itemGroup) {
        T item = function.apply(properties.setId(identifier));
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);
        return item;
    }

    public static class ItemDataReloadListener implements SimpleSynchronousResourceReloadListener {
        static private boolean printedInfo = false;

        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "items");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/item", path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    ItemData data = Json.GSON.fromJson(reader, ItemData.class);
                    ItemRegistry.register(data);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load item resource \"" + entry.getKey() + "\".");
                }
            }

            if (!printedInfo) {
                Filament.LOGGER.info("filament items registered: " + ItemRegistry.REGISTERED_ITEMS);
                Filament.LOGGER.info("filament blocks registered: " + BlockRegistry.REGISTERED_BLOCKS);
                Filament.LOGGER.info("filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS);
                Filament.LOGGER.info("filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES);
                printedInfo = true;
            }
        }
    }
}
