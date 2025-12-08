package de.tomalbrc.filament.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.Filament;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class TextUtil {
    public static Component formatText(String text) {
        if (text == null || text.isBlank())
            return Component.empty();

        if (FilamentConfig.getInstance().minimessage) {
            var parsed = MiniMessage.miniMessage().deserialize(text);
            var adventure = Filament.adventure();
            return adventure == null ? toNative(parsed) : adventure.toNative(parsed);
        }
        else
            return TagParser.SIMPLIFIED_TEXT_FORMAT.parseText(text, ParserContext.of());
    }

    private static Component toNative(net.kyori.adventure.text.Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        JsonElement jsonElement = JsonParser.parseString(json);
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).result().orElse(Component.empty());
    }
}
