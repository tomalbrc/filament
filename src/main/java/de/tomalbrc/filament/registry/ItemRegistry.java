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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

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

        SimpleItem item = new SimpleItem(null, properties, data, data.vanillaItem());

        BehaviourUtil.postInitItem(item, item, data.behaviourConfig());

        registerItem(data.id(), item, data.itemGroup() != null ? data.itemGroup() : Constants.ITEM_GROUP_ID);

        REGISTERED_ITEMS++;
    }

    public static void registerItem(ResourceLocation identifier, Item item, ResourceLocation itemGroup) {
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);
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
                    Filament.LOGGER.error("Failed to load item resource \"{}\".", entry.getKey(), e);
                }
            }

            if (!printedInfo) {
                for (String s : Arrays.asList("Filament items registered: " + REGISTERED_ITEMS, "Filament blocks registered: " + BlockRegistry.REGISTERED_BLOCKS, "Filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS, "Filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES)) {
                    Filament.LOGGER.info(s);
                }
                printedInfo = true;
            }
        }
    }
}