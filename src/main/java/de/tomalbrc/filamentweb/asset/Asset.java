package de.tomalbrc.filamentweb.asset;

import com.google.gson.JsonElement;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import de.tomalbrc.filament.util.Json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class Asset {
    public UUID uuid;
    public Path path;
    public Data<?> data;
    public Type type;

    private JsonElement raw;

    protected JsonElement schema;

    public JsonElement getSchema() {
        if (schema == null) {
            schema = AssetStore.getSchema(data, type);
        }

        return schema;
    }

    public JsonElement readJson() {
        if (this.raw == null) {
            raw = Json.GSON.toJsonTree(data);
        }

        return raw;
    }

    public void apply(JsonElement json) {
        this.raw = json;
        try {
            data = Json.GSON.fromJson(json, this.data.getClass());
            this.schema = AssetStore.getSchema(data, type);
        } catch (Exception e) {
            Filament.LOGGER.error("Could not apply changes to {}! ", data.id(), e);
        }
    }

    public boolean writeFile() {
        if (path == null || data == null)
            return false;

        try {
            String content = Json.GSON.toJson(data);
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Filament.LOGGER.error("Could not write asset json", e);
            return false;
        }

        return true;
    }

    public void runtimeRegister() {
        try {
            if (type == ItemData.class) {
                ItemRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            } else if (type == BlockData.class) {
                BlockRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            } else if (type == DecorationData.class) {
                DecorationRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            Filament.LOGGER.error("Could not re-register data for {}", data.id(), e);
        }
    }

    public String icon() {
        if (type == BlockData.class) {
            return "🧱";
        } else if (type == DecorationData.class) {
            return "🖼";
        }
        return "📦";
    }
}
