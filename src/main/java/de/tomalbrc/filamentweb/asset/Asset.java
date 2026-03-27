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
import de.tomalbrc.filament.util.mixin.RegistryUnfreezer;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

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

    public JsonElement schema;

    public JsonElement readJson() {
        return Json.GSON.toJsonTree(data);
    }

    public void apply(JsonElement json) {
        this.data = Json.GSON.fromJson(json, data.getClass());
        this.schema = AssetStore.getSchema(data, type);
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
            Filament.LOGGER.error("Could not register data", e);
        }
    }
}
