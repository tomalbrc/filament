package de.tomalbrc.filament.registry;

import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.*;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static Map<ResourceLocation, List<ResourceLocation>> ITEMS_TAGS = new Object2ReferenceOpenHashMap<>();
    public static Map<Item, Item> COPY_TAGS = new Reference2ReferenceOpenHashMap<>(); // custom->vanilla

    public static void register(InputStream inputStream) throws IOException {
        var element = JsonParser.parseReader(new InputStreamReader(inputStream));
        ItemData data = Json.GSON.fromJson(element, ItemData.class);

        Util.handleComponentsCustom(element, data);

        register(data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static public void register(ItemData data) {
        if (BuiltInRegistries.ITEM.containsKey(data.id())) {
            var item = BuiltInRegistries.ITEM.getValue(data.id());
            if (item instanceof FilamentItem filamentItem) {
                filamentItem.initBehaviours(data.behaviour());
                postRegistration(filamentItem, data);
            }
            return;
        }

        Item.Properties properties = data.properties().toItemProperties();

        if (data.properties().copyComponents) {
            for (TypedDataComponent component : data.vanillaItem().components()) {
                properties.component(component.type(), component.value());
            }
        }

        for (TypedDataComponent component : data.components()) {
            properties.component(component.type(), component.value());
        }

        var item = ItemRegistry.registerItem(key(data.id()), (newProps) -> new SimpleItem(newProps, data, data.vanillaItem()), properties, data.group() != null ? data.group() : Constants.ITEM_GROUP_ID, data.itemTags());
        postRegistration(item, data);

        if (data.properties().copyTags) {
            COPY_TAGS.put(item, data.vanillaItem());
        }

        FilamentRegistrationEvents.ITEM.invoker().registered(data, item);
    }

    static void postRegistration(FilamentItem item, ItemData data) {
        BehaviourUtil.postInitItem(item.asItem(), item, data.behaviour());
        Translations.add(item.asItem(), null, data);
        RPUtil.create(item, data);
    }

    public static ResourceKey<Item> key(ResourceLocation id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    public static <T extends Item> T registerItem(ResourceKey<Item> identifier, Function<Item.Properties, T> function, Item.Properties properties, ResourceLocation itemGroup, @Nullable Collection<ResourceLocation> tags) {
        T item = function.apply(properties.setId(identifier));
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);

        if (tags != null) for (ResourceLocation tag : tags) {
            var list = ITEMS_TAGS.computeIfAbsent(tag, x -> new ArrayList<>());
            list.add(identifier.location());
        }

        return item;
    }

    public static class ItemDataReloadListener implements FilamentSynchronousResourceReloadListener {
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
        }
    }
}