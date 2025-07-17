package de.tomalbrc.filament.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component formatText(String text) {
        if (FilamentConfig.getInstance().minimessage) {
            return NonWrappingComponentSerializer.INSTANCE.serialize(MiniMessage.miniMessage().deserialize(text));
        } else {
            return TagParser.SIMPLIFIED_TEXT_FORMAT.parseText(text, ParserContext.of());
        }
    }
}
