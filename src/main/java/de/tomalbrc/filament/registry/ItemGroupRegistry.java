package de.tomalbrc.filament.registry;

import com.google.gson.reflect.TypeToken;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.ItemGroupData;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import de.tomalbrc.filament.util.Json;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ItemGroupRegistry {
    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, List<Item>> TAB_GROUP_ITEMS = new Object2ObjectLinkedOpenHashMap<>();
    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, CreativeModeTab> TAB_GROUPS = new Object2ObjectLinkedOpenHashMap<>();

    public static void register(InputStream inputStream) throws IOException {
        List<ItemGroupData> data = Json.GSON.fromJson(new InputStreamReader(inputStream), TypeToken.getParameterized(List.class, ItemGroupData.class).getType());
        for (ItemGroupData datum : data) {
            register(datum);
        }
    }

    static public void register(ItemGroupData data) {
        if (TAB_GROUPS.containsKey(data.id())) return;

        CreativeModeTab group = new CreativeModeTab.Builder(null, -1)
                .title(data.literal() == null ? Component.translatable(data.id().getNamespace()+".itemGroup."+data.id().getPath()) : TextParserUtils.formatNodesSafe(data.literal()).toText())
                .icon(() -> BuiltInRegistries.ITEM.getValue(data.item()).getDefaultInstance())
                .displayItems((parameters, output) -> TAB_GROUP_ITEMS.get(data.id()).forEach(output::accept))
                .build();

        TAB_GROUPS.put(data.id(), group);
        TAB_GROUP_ITEMS.putIfAbsent(data.id(), new ObjectArrayList<>());

        if (!PolymerItemGroupUtils.contains(data.id()) && !TAB_GROUP_ITEMS.get(data.id()).isEmpty()) {
            PolymerItemGroupUtils.registerPolymerItemGroup(data.id(), group);
        }
    }

    public static void addItem(ResourceLocation identifier, Item item) {
        TAB_GROUP_ITEMS.putIfAbsent(identifier, new ObjectArrayList<>());
        TAB_GROUP_ITEMS.get(identifier).add(item);

        if (!PolymerItemGroupUtils.contains(identifier) && TAB_GROUPS.containsKey(identifier)) {
            PolymerItemGroupUtils.registerPolymerItemGroup(identifier, TAB_GROUPS.get(identifier));
        }
    }

    public static class ItemGroupDataReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item_groups");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            load(Constants.MOD_ID, "item-groups", resourceManager, (id, inputStream) -> {
                try {
                    ItemGroupRegistry.register(inputStream);
                } catch (IOException e) {
                    Filament.LOGGER.error("Failed to load item group config \"{}\".", id);
                }
            });
        }
    }
}
