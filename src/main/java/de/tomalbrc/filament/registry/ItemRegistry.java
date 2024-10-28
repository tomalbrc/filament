package de.tomalbrc.filament.registry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static int REGISTERED_ITEMS = 0;

    public static void register(InputStream inputStream) throws IOException {
        var element = JsonParser.parseReader(new InputStreamReader(inputStream));
        register(Json.GSON.fromJson(element, ItemData.class), element);
    }

    static public void register(ItemData data, JsonElement element) {
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
                    JsonElement element = JsonParser.parseReader(reader);
                    ItemData data = Json.GSON.fromJson(element, ItemData.class);

                    if (element.getAsJsonObject().has("components")) {
                        JsonObject comp = element.getAsJsonObject().get("components").getAsJsonObject();
                        if (comp.has("minecraft:jukebox_playable")) {
                            data.set(DataComponents.JUKEBOX_PLAYABLE, comp.getAsJsonObject("minecraft:jukebox_playable"));
                        }
                        if (comp.has("jukebox_playable")) {
                            data.set(DataComponents.JUKEBOX_PLAYABLE, comp.getAsJsonObject("jukebox_playable"));
                        }
                    }

                    ItemRegistry.register(data, element);
                    BlockRegistry.addLate();
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