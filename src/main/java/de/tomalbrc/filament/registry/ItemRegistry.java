package de.tomalbrc.filament.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.behaviour.BehaviourUtil;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.injection.DataComponentCopying;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.util.*;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static Map<Identifier, List<Identifier>> ITEMS_TAGS = new Object2ReferenceOpenHashMap<>();
    public static Map<Item, Item> COPY_TAGS = new Reference2ReferenceOpenHashMap<>(); // custom->vanilla

    public static void register(Path filepath, InputStream inputStream) throws IOException {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(inputStream));
        try {
            ItemData data = Json.GSON.fromJson(element, ItemData.class);
            data.filepath = filepath;
            Util.handleComponentsCustom(element, data);
            register(data);
        } catch (Exception e) {
            Filament.LOGGER.error("Could not load file! Error: {}", String.valueOf(e.fillInStackTrace()));
            Filament.LOGGER.info("Path: {}", filepath);
            Filament.LOGGER.info("File: \n{}", element.toString());
        }
    }

    public static void register(InputStream inputStream) throws IOException {
        register(null, inputStream);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static public void register(ItemData data) {
        if (BuiltInRegistries.ITEM.containsKey(data.id())) {
            var item = BuiltInRegistries.ITEM.getValue(data.id());
            if (item.isFilamentItem()) {
                item.asFilamentItem().initBehaviours(data.behaviour());
                postRegistration(item.asFilamentItem(), data);
            }
            return;
        }

        Item.Properties properties = data.properties().toItemProperties();

        var item = ItemRegistry.registerItem(key(data.id()), (newProps) -> new SimpleItem(newProps, data, data.vanillaItem()), properties, data.group() != null ? data.group() : Constants.ITEM_GROUP_ID, data.itemTags());
        postRegistration(item, data);

        ((DataComponentCopying)BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).filament$registerToCopy(new DataComponentCopying.CustomInitializerEntry(item.builtInRegistryHolder().key(), data.vanillaItem().builtInRegistryHolder().key(), (vanillaInitializer, target, provider)-> {
            if (vanillaInitializer != null && data.properties().copyComponents == Boolean.TRUE) {
                var tempBuilder = DataComponentMap.builder();
                vanillaInitializer.run(tempBuilder, provider);
                var built = tempBuilder.build();
                for (TypedDataComponent component : built) {
                    if (!target.contains(component.type()) || DataComponents.COMMON_ITEM_COMPONENTS.has(component.type())) {
                        target.set(component.type(), component.value());
                    }
                }
            }

            for (TypedDataComponent component : data.components()) {
                target.set(component.type(), component.value());
            }
        }));

        if (data.properties().copyTags == Boolean.TRUE) {
            COPY_TAGS.put(item, data.vanillaItem());
        }

        FilamentRegistrationEvents.ITEM.invoker().registered(data, item);
    }

    static void postRegistration(FilamentItem item, ItemData data) {
        BehaviourUtil.postInitItem(item.asItem(), item, data.behaviour());
        Translations.add(item.asItem(), null, data);
        RPUtil.create(item, data);
    }

    public static ResourceKey<Item> key(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    public static <T extends Item> T registerItem(ResourceKey<Item> identifier, Function<Item.Properties, T> function, Item.Properties properties, Identifier itemGroup, @Nullable Collection<Identifier> tags) {
        T item = function.apply(properties.setId(identifier));
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        ItemGroupRegistry.addItem(itemGroup, item);

        if (tags != null) for (Identifier tag : tags) {
            var list = ITEMS_TAGS.computeIfAbsent(tag, x -> new ArrayList<>());
            list.add(identifier.identifier());
        }

        return item;
    }

    public static class ItemDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "items");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load("filament/item", null, resourceManager, (id, inputStream) -> {
                try {
                    ItemRegistry.register(obtainPath(id, resourceManager), inputStream);
                } catch (Exception e) {
                    Filament.LOGGER.error("Failed to load item resource \"{}\".", id, e);
                }
            });
        }
    }
}