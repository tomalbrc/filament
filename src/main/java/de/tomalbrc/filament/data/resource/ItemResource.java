package de.tomalbrc.filament.data.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.tomalbrc.filament.util.annotation.AssetRef;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemResource implements ResourceProvider {
    private Map<String, @AssetRef(AssetRef.Type.MODEL) Identifier> models = new Object2ObjectArrayMap<>();

    private Identifier parent;
    private Map<String, Map<String, @AssetRef(AssetRef.Type.TEXTURE) Identifier>> textures;

    public ItemResource() {}

    public ItemResource(Map<String, Identifier> models, Identifier parent, Map<String, Map<String, Identifier>> textures) {
        this.models = models;
        this.parent = parent;
        this.textures = textures;
    }

    public Identifier parent() {
        if (parent == null)
            parent = Identifier.withDefaultNamespace("item/generated");
        return parent;
    }

    public Map<String, Map<String, Identifier>> textures() {
        return textures;
    }

    public boolean couldGenerate() {
        return parent != null && textures != null;
    }

    @Override
    public Map<String, Identifier> getModels() {
        return this.models;
    }

    public static class Serializer implements JsonSerializer<ItemResource> {
        @Override
        public JsonElement serialize(ItemResource src, Type typeOfSrc, JsonSerializationContext context) {
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
