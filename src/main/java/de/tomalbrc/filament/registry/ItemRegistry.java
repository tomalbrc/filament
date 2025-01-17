package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filament.util.RPUtil;
import de.tomalbrc.filament.util.Translations;
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

        if (data.properties().copyComponents) {
            for (TypedDataComponent component : data.vanillaItem().components()) {
                properties.component(component.type(), component.value());
            }
        }

        for (TypedDataComponent component : data.components()) {
            properties.component(component.type(), component.value());
        }

        SimpleItem item = new SimpleItem(null, properties, data, data.vanillaItem());
        BehaviourUtil.postInitItem(item, item, data.behaviourConfig());
        Translations.add(item, null, data);

        registerItem(data.id(), item, data.itemGroup() != null ? data.itemGroup() : Constants.ITEM_GROUP_ID);
        RPUtil.create(item, data.id(), data.itemResource());

        REGISTERED_ITEMS++;
    }

    public static void registerItem(ResourceLocation identifier, Item item, ResourceLocation itemGroup) {
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);
    }

    public static class ItemDataReloadListener implements FilamentSynchronousResourceReloadListener {
        static private boolean printedInfo = false;

        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "items");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/item", null, resourceManager, (id, inputStream) -> {
                try {
                    ItemRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load item resource \"{}\".", id, e);
                }
            });
            if (!printedInfo) {
                for (String s : Arrays.asList("Filament items registered: " + REGISTERED_ITEMS, "Filament blocks registered: " + BlockRegistry.REGISTERED_BLOCKS, "Filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS, "Filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES)) {
                    Filament.LOGGER.info(s);
                }
                printedInfo = true;
            }
        }
    }
}