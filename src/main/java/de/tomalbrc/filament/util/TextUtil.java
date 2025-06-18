package de.tomalbrc.filament.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component formatText(String text) {
        return TagParser.QUICK_TEXT.parseText(text, ParserContext.of());
    }
}
