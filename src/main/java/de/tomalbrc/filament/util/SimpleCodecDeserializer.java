package de.tomalbrc.filament.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public class SimpleCodecDeserializer<T> implements JsonDeserializer<T>, JsonSerializer<T> {

    private final Codec<T> codec;

    public SimpleCodecDeserializer(Codec<T> codec) {
        this.codec = codec;

    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return codec.parse(JsonOps.INSTANCE, json)
                .getOrThrow(error ->
                        new JsonParseException("Failed to deserialize using Codec: " + error)
                );
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        return codec.encodeStart(JsonOps.INSTANCE, t).getOrThrow(error ->
                new JsonParseException("Failed to serialize using Codec: " + error)
        );
    }
}