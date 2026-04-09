package de.tomalbrc.filament.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component formatText(String text) {
        if (text == null || text.isBlank())
            return Component.empty();

        //if (FilamentConfig.getInstance().minimessage) {
        //    var parsed = MiniMessage.miniMessage().deserialize(text);
        //    var adventure = Filament.adventure();
        //    return adventure == null ? toNative(parsed) : adventure.asNative(parsed);
        //}
        else
            return FilamentFormatter.parse(text);
    }

    //private static Component toNative(net.kyori.adventure.text.Component component) {
    //    String json = GsonComponentSerializer.gson().serialize(component);
    //    JsonElement jsonElement = JsonParser.parseString(json);
    //    return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).result().orElse(Component.empty());
    //}
}
