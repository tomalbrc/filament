package de.tomalbrc.filament.registry;

import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.generator.ItemAssetGenerator;
import de.tomalbrc.filament.item.SimpleItem;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static int REGISTERED_ITEMS = 0;

    public static void register(InputStream inputStream) throws IOException {
        var element = JsonParser.parseReader(new InputStreamReader(inputStream));
        ItemData data = Json.GSON.fromJson(element, ItemData.class);

        Util.handleComponentsCustom(element, data);

        register(data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static public void register(ItemData data) {
        if (BuiltInRegistries.ITEM.containsKey(data.id())) return;

        Item.Properties properties = data.properties().toItemProperties(data.behaviour());

        for (TypedDataComponent component : data.components()) {
            properties.component(component.type(), component.value());
        }

        var item = ItemRegistry.registerItem(key(data.id()), (newProps) -> new SimpleItem(null, newProps, data, data.vanillaItem()), properties, data.group() != null ? data.group() : Constants.ITEM_GROUP_ID);
        BehaviourUtil.postInitItem(item, item, data.behaviour());
        Translations.add(item, data);

        var itemResources = data.itemResource();
        if (itemResources != null && data.itemModel() == null && itemResources.getModels() != null) {
            PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder ->
                    ItemAssetGenerator.create(resourcePackBuilder, data.id(), itemResources, data.vanillaItem().components().has(DataComponents.DYED_COLOR))
            );
        }

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
                try (InputStream inputStream = entry.getValue().open()) {
                    ItemRegistry.register(inputStream);
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