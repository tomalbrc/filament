package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import de.tomalbrc.filament.data.Data;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Translations {
    // locale => Pair<Item, Block> => text (x2)
    private static final Map<String, Map<Pair<Item, Block>, String>> translations = new Object2ObjectArrayMap<>();

    public static void add(Item item, Block block, Data data) {
        if (data.translations() != null) {
            for (Map.Entry<String, String> entry : Objects.requireNonNull(data.translations()).entrySet()) {
                Translations.add(entry.getKey(), Pair.of(item, block), entry.getValue());
            }
        }
    }

    private static void add(String locale, Pair<Item, Block> translationId, String text) {
        translations.putIfAbsent(locale, new Object2ObjectArrayMap<>());
        translations.get(locale).put(translationId, text);
    }

    public static void init() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            for (Map.Entry<String, Map<Pair<Item, Block>, String>> entry : translations.entrySet()) {
                JsonObject object = new JsonObject();
                for (Map.Entry<Pair<Item, Block>, String> idTextEntry : entry.getValue().entrySet()) {
                    var pair = idTextEntry.getKey();
                    if (pair.getFirst() != null) {
                        object.add(pair.getFirst().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                    }
                    if (pair.getSecond() != null) {
                        object.add(pair.getSecond().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                    }
                }

                var str = Json.GSON.toJson(object);
                resourcePackBuilder.addData("assets/filament/lang/" + entry.getKey() + ".json", str.getBytes(StandardCharsets.UTF_8));
            }
        });
    }
}
