package de.tomalbrc.filament.data.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.tomalbrc.filament.util.annotation.AssetRef;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class BlockResource implements ResourceProvider {
    private Map<String, PolymerBlockModel> models = new Object2ObjectArrayMap<>();
    private Identifier parent;
    private Map<String, TextureBlockModel> textures;

    public BlockResource(Map<String, PolymerBlockModel> models) {
        this(models, null, null);
    }

    public BlockResource(Map<String, PolymerBlockModel> models, Identifier parent, Map<String, TextureBlockModel> textures) {
        this.models = models;
        this.parent = parent;
        this.textures = textures;
    }

    public Identifier parent() {
        if (parent == null)
            parent = Identifier.withDefaultNamespace("block/cube_all");
        return parent;
    }

    public Map<String, TextureBlockModel> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return parent != null && textures != null;
    }

    @Override
    public Map<String, Identifier> getModels() {
        if (this.models == null || this.models.isEmpty()) return Collections.emptyMap();

        var map = new Object2ObjectOpenHashMap<String, Identifier>(this.models.size());

        for (var entry : this.models.entrySet()) {
            String rawKey = entry.getKey();
            String processedKey = rawKey;

            if (rawKey.indexOf(',') != -1) {
                String[] parts = rawKey.split(",");
                Arrays.sort(parts);
                processedKey = String.join(",", parts);
            }

            map.put(processedKey, entry.getValue().model());
        }
        return map;
    }

    public Map<String, PolymerBlockModel> models() {
        return this.models;
    }

    public void addModel(String key, PolymerBlockModel blockModel) {
        if (this.models == null) this.models = new Object2ObjectArrayMap<>();
        this.models.put(key, blockModel);
    }

    public record TextureBlockModel(Map<String, @AssetRef(AssetRef.Type.TEXTURE) Identifier> textures, int x, int y, boolean uvlock, int weight) {}

    public static class Serializer implements JsonSerializer<BlockResource> {
        @Override
        public JsonElement serialize(BlockResource src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            if (src.parent() != null && !src.parent().getPath().isEmpty() && src.textures() != null && !src.textures().isEmpty()) {
                json.addProperty("parent", src.parent().toString());
                json.add("textures", context.serialize(src.textures()));
            } else if (src.getModels() != null && !src.getModels().isEmpty()) {
                json.add("models", context.serialize(src.getModels()));
            }

            return json;
        }
    }
}