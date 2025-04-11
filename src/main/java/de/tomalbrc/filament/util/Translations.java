package de.tomalbrc.filament.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.entity.EntityData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Triple;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Translations {
    // locale => Pair<Item, Block> => text (x2)
    private static final Map<String, Map<Triple<Item, Block, EntityType<?>>, String>> translations = new Object2ObjectArrayMap<>();

    public static void add(EntityType<?> entity, EntityData data) {
        if (data.translations() != null) {
            for (Map.Entry<String, String> entry : Objects.requireNonNull(data.translations()).entrySet()) {
                Translations.add(entry.getKey(), Triple.of(null, null, entity), entry.getValue());
            }
        }
    }

    public static void add(Item item, Block block, Data data) {
        if (data.translations() != null) {
            for (Map.Entry<String, String> entry : Objects.requireNonNull(data.translations()).entrySet()) {
                Translations.add(entry.getKey(), Triple.of(item, block, null), entry.getValue());
            }
        }
    }

    private static void add(String locale, Triple<Item, Block, EntityType<?>> translationId, String text) {
        translations.putIfAbsent(locale, new Object2ObjectArrayMap<>());
        translations.get(locale).put(translationId, text);
    }

    public static void registerEventHandler() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            for (Map.Entry<String, Map<Triple<Item, Block, EntityType<?>>, String>> entry : translations.entrySet()) {
                JsonObject object = new JsonObject();
                for (Map.Entry<Triple<Item, Block, EntityType<?>>, String> idTextEntry : entry.getValue().entrySet()) {
                    var pair = idTextEntry.getKey();
                    if (pair.getLeft() != null) {
                        object.add(pair.getLeft().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                    }
                    if (pair.getMiddle() != null) {
                        object.add(pair.getMiddle().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                    }
                    if (pair.getRight() != null) {
                        object.add(pair.getRight().getDescriptionId(), new JsonPrimitive(idTextEntry.getValue()));
                    }
                }

                var str = Json.GSON.toJson(object);
                resourcePackBuilder.addData("assets/filament/lang/" + entry.getKey() + ".json", str.getBytes(StandardCharsets.UTF_8));
            }
        });
    }
}
