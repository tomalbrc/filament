package de.tomalbrc.filamentweb.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.io.*;
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
    private JsonElement cachedSchema;

    boolean dirty = false;

    public JsonElement getSchema() {
        if (this.cachedSchema == null) {
            this.cachedSchema = AssetStore.generateSchema(data, type);
        }
        return this.cachedSchema;
    }

    public JsonElement getJson() {
        if (this.raw == null) {
            raw = Json.GSON.toJsonTree(data);
        }
        return raw;
    }

    public JsonElement readJson() {
        if (this.raw == null) {
            var p = FabricLoader.getInstance().getGameDir().resolve(path);
            try {
                raw = JsonParser.parseReader(new InputStreamReader(new FileInputStream(p.toFile())));
            } catch (FileNotFoundException e) {
                Filament.LOGGER.error("Could not read file at {} - falling back to in-memory file", path, e);
                raw = Json.GSON.toJsonTree(data);
            }
        }

        return raw;
    }

    public void apply(JsonElement json) {
        this.raw = json;
        try {
            this.data = Json.GSON.fromJson(json, this.data.getClass());
            Filament.LOGGER.info("Applied JSON data successfully for {}!", data.id());
        } catch (Exception e) {
            Filament.LOGGER.error("Could not apply changes to {}! ", data.id(), e);
        }
        setDirty(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) {
            this.cachedSchema = null;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean writeFile() {
        if (path == null || data == null)
            return false;

        try {
            String content = Json.GSON.toJson(data);
            Files.writeString(path, content, StandardCharsets.UTF_8);
            setDirty(false);
            Filament.LOGGER.info("Wrote file {}!", path);
        } catch (IOException e) {
            Filament.LOGGER.error("Could not write asset json at {}", path, e);
            return false;
        }

        return true;
    }

    private void remove(Registry reg, Identifier identifier) {
        var block = reg.getValue(identifier);
        var key = reg.getResourceKey(block);
        if (key.isPresent()) ((RegistryUnfreezer) reg).filament$hackyRemove(block, (ResourceKey) key.get());
    }

    public void runtimeRegister() {
        writeFile();

        try {
            // omegacursed

            ((RegistryUnfreezer) BuiltInRegistries.BLOCK).filament$unfreeze();
            ((RegistryUnfreezer) BuiltInRegistries.ITEM).filament$unfreeze();
            ((RegistryUnfreezer) BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$unfreeze();
            ((RegistryUnfreezer) BuiltInRegistries.CREATIVE_MODE_TAB).filament$unfreeze();

            remove(BuiltInRegistries.ITEM, data.id());
            remove(BuiltInRegistries.BLOCK, data.id());
            remove(BuiltInRegistries.BLOCK_ENTITY_TYPE, data.id());

            if (type == ItemData.class) {
                ItemRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            } else if (type == BlockData.class) {
                BlockRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            } else if (type == DecorationData.class) {
                DecorationRegistry.register(path, new ByteArrayInputStream(Json.GSON.toJson(data).getBytes(StandardCharsets.UTF_8)));
            }

            ((RegistryUnfreezer) BuiltInRegistries.BLOCK).filament$freeze();
            ((RegistryUnfreezer) BuiltInRegistries.ITEM).filament$freeze();
            ((RegistryUnfreezer) BuiltInRegistries.CREATIVE_MODE_TAB).filament$freeze();
            ((RegistryUnfreezer) BuiltInRegistries.BLOCK_ENTITY_TYPE).filament$freeze();

            ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$reorderEntries();
            ((PolymerIdMapper<?>) Fluid.FLUID_STATE_REGISTRY).polymer$reorderEntries();

            for (DataComponentInitializers.PendingComponents<?> pendingComponents : BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(Filament.SERVER.registryAccess())) {
                pendingComponents.apply();
            }

            Filament.LOGGER.info("Re-registered {}!", data.id());

        } catch (Exception e) {
            Filament.LOGGER.error("Could not re-register data for {}", data.id(), e);
            Filament.LOGGER.error(e.toString());
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
