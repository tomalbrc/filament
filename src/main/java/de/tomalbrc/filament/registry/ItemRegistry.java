package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.item.InstrumentItem;
import de.tomalbrc.filament.item.SimpleItem;
import de.tomalbrc.filament.item.ThrowingItem;
import de.tomalbrc.filament.item.TrapItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.io.FileUtils;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;

public class ItemRegistry {
    public static int REGISTERED_ITEMS = 0;

    public static final File DIR = Constants.CONFIG_DIR.resolve("item").toFile();

    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, Item> CUSTOM_ITEMS = new Object2ObjectLinkedOpenHashMap<>();
    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, Item> CUSTOM_BLOCK_ITEMS = new Object2ObjectLinkedOpenHashMap<>();
    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, Item> CUSTOM_DECORATIONS = new Object2ObjectLinkedOpenHashMap<>();
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
            .title(Component.literal("Filament Items").withStyle(ChatFormatting.AQUA))
            .icon(Items.DIAMOND::getDefaultInstance)
            .displayItems((parameters, output) -> CUSTOM_ITEMS.forEach((key, value) -> output.accept(value)))
            .build();

    public static final CreativeModeTab BLOCK_ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
            .title(Component.literal("Filament Blocks").withStyle(ChatFormatting.DARK_BLUE))
            .icon(Items.FURNACE::getDefaultInstance)
            .displayItems((parameters, output) -> CUSTOM_BLOCK_ITEMS.forEach((key, value) -> output.accept(value)))
            .build();

    public static final CreativeModeTab DECORATION_ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
            .title(Component.literal("Filament Decoration").withStyle(ChatFormatting.DARK_PURPLE))
            .icon(Items.LANTERN::getDefaultInstance)
            .displayItems((parameters, output) -> CUSTOM_DECORATIONS.forEach((key, value) -> output.accept(value)))
            .build();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(new ResourceLocation(Constants.MOD_ID, "item"), ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(new ResourceLocation(Constants.MOD_ID, "block"), BLOCK_ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(new ResourceLocation(Constants.MOD_ID, "decoration"), DECORATION_ITEM_GROUP);

        if (!DIR.exists() || !DIR.isDirectory()) {
            DIR.mkdirs();
            return;
        }

        Collection<File> files = FileUtils.listFiles(DIR, new String[]{"json"}, true);
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    ItemData data = Json.GSON.fromJson(reader, ItemData.class);

                    Item.Properties properties = data.properties() != null ? data.properties().toItemProperties(data.vanillaItem(), data.behaviour()) : new Item.Properties();
                    Item item;
                    if (data.canShoot()) {
                        item = new ThrowingItem(properties, data);
                    } else if (data.isInstrument()) {
                        item = new InstrumentItem(properties, data);
                    } else if (data.isTrap()) {
                        item = new TrapItem(properties, data);
                    } else {
                        item = new SimpleItem(properties, data);
                    }

                    ItemRegistry.registerItem(data.id(), item, CUSTOM_ITEMS);
                    REGISTERED_ITEMS++;

                } catch (Throwable throwable) {
                    Filament.LOGGER.error("Error reading item JSON file: {}", file.getAbsolutePath(), throwable);
                }
            }
        }
    }

    public static void registerItem(ResourceLocation identifier, Item item, Object2ObjectLinkedOpenHashMap<ResourceLocation, Item> CAT) {
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        CAT.putIfAbsent(identifier, item);
    }
}
