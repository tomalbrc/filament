package de.tomalbrc.filament.util;

import de.tomalbrc.filament.Filament;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component formatText(String text) {
        if (text == null || text.isBlank())
            return Component.empty();

        if (FilamentConfig.getInstance().minimessage) {
            var parsed = MiniMessage.miniMessage().deserialize(text);
            return Filament.adventure().toNative(parsed);
        }
        else
            return TagParser.SIMPLIFIED_TEXT_FORMAT.parseText(text, ParserContext.of());
    }
}
