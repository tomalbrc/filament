package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.*;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ItemRegistry {
    public static Map<ResourceLocation, Collection<ResourceLocation>> ITEMS_TAGS = new Object2ReferenceOpenHashMap<>();

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

        registerItem(data.id(), item, data.itemGroup() != null ? data.itemGroup() : Constants.ITEM_GROUP_ID, data.itemTags());
        RPUtil.create(item, data.id(), data.itemResource());

        FilamentRegistrationEvents.ITEM.invoker().registered(data, item);
    }

    public static void registerItem(ResourceLocation identifier, Item item, ResourceLocation itemGroup, @Nullable Collection<ResourceLocation> tags) {
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);
        ITEMS_TAGS.put(identifier, tags);
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
                for (String s : Arrays.asList("Filament items registered: " + ITEMS_TAGS.size(), "Filament blocks registered: " + BlockRegistry.BLOCKS_TAGS.size(), "Filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS, "Filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES)) {
                    Filament.LOGGER.info(s);
                }
                printedInfo = true;
            }
        }
    }
}