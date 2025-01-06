package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.data.Data;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Translations {
    // locale => item => text
    private static final Map<String, Map<Item, String>> translations = new Object2ObjectArrayMap<>();
    private static final Map<String, Map<Block, String>> blockTranslations = new Object2ObjectArrayMap<>();

    public static void add(Item item, Data data) {
        if (data.translations() != null) for (Map.Entry<String, String> entry : Objects.requireNonNull(data.translations()).entrySet()) {
            Translations.add(entry.getKey(), item, entry.getValue());
        }
    }

    public static void add(Item item, Block block, Data data) {
        if (data.translations() != null) {
            for (Map.Entry<String, String> entry : Objects.requireNonNull(data.translations()).entrySet()) {
                Translations.add(entry.getKey(), item, entry.getValue());
                Translations.add(entry.getKey(), block, entry.getValue());
            }
        }
    }

    private static void add(String locale, Item translationId, String text) {
        translations.putIfAbsent(locale, new Object2ObjectArrayMap<>());
        translations.get(locale).put(translationId, text);
    }

    private static void add(String locale, Block translationId, String text) {
        blockTranslations.putIfAbsent(locale, new Object2ObjectArrayMap<>());
        blockTranslations.get(locale).put(translationId, text);
    }

    public static void init() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            for (Map.Entry<String, Map<Item, String>> entry : translations.entrySet()) {
                JsonObject object = new JsonObject();
                for (Map.Entry<Item, String> idTextEntry : entry.getValue().entrySet()) {
                    object.add(idTextEntry.getKey().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                }
                var str = Json.GSON.toJson(object);
                resourcePackBuilder.addData("assets/filament/lang/" + entry.getKey() + ".json", str.getBytes(StandardCharsets.UTF_8));
            }

            for (Map.Entry<String, Map<Block, String>> entry : blockTranslations.entrySet()) {
                JsonObject object = new JsonObject();
                for (Map.Entry<Block, String> idTextEntry : entry.getValue().entrySet()) {
                    object.add(idTextEntry.getKey().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                }
                var str = Json.GSON.toJson(object);
                resourcePackBuilder.addData("assets/filament/lang/" + entry.getKey() + ".json", str.getBytes(StandardCharsets.UTF_8));
            }
        });
    }
}
