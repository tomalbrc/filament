package de.tomalbrc.filament.registry;

import com.google.gson.*;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class Templates {
    private static final Map<ResourceLocation, JsonObject> TEMPLATES = new Object2ObjectOpenHashMap<>();

    public static void add(InputStream inputStream) throws Exception {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(Json.camelToSnakeCase(inputStream)));
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            TEMPLATES.put(ResourceLocation.parse(object.getAsJsonPrimitive("id").getAsString()), object);
        }
    }

    public static JsonElement handlePlaceholder(JsonElement element, ResourceLocation id) {
        if (element.isJsonObject()) {
            JsonObject obj = new JsonObject();
            for (var entry : element.getAsJsonObject().entrySet()) {
                obj.add(entry.getKey(), handlePlaceholder(entry.getValue(), id));
            }
            return obj;
        } else if (element.isJsonArray()) {
            JsonArray arr = new JsonArray();
            for (var v : element.getAsJsonArray()) {
                arr.add(handlePlaceholder(v, id));
            }
            return arr;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isString()) {
                String value = prim.getAsString();
                if (value.contains("<item_id_capitalized>") || value.contains("<item_id>")) {
                    String replaced = value
                            .replace("<item_id_capitalized>", capitalizeWords(id.getPath()))
                            .replace("<item_namespace>", id.getNamespace())
                            .replace("<item_id>", id.getPath());
                    return new JsonPrimitive(replaced);
                }
            }
            return prim.deepCopy();
        } else {
            return element.deepCopy();
        }
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        for (String word : input.replace("_", " ").split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static JsonObject merge(ResourceLocation templateId, ResourceLocation contentId, JsonObject json2) {
        JsonObject template = TEMPLATES.get(templateId);
        if (template == null)
            throw new IllegalArgumentException(String.format("Template id '%s' not found for '%s'!", templateId, contentId));

        JsonObject result = template.deepCopy();

        for (Map.Entry<String, JsonElement> e : json2.entrySet()) {
            String key = e.getKey();
            JsonElement value2 = e.getValue().deepCopy();

            if (result.has(key) && result.get(key).isJsonObject() && value2.isJsonObject()) {
                JsonObject merged = mergeJsonObject(result.getAsJsonObject(key), value2.getAsJsonObject());
                result.add(key, merged);
            } else {
                result.add(key, value2);
            }
        }

        return result;
    }

    private static JsonObject mergeJsonObject(JsonObject obj1, JsonObject obj2) {
        JsonObject merged = obj1.deepCopy();

        for (Map.Entry<String, JsonElement> e : obj2.entrySet()) {
            String key = e.getKey();
            JsonElement value2 = e.getValue().deepCopy();

            if (merged.has(key)) {
                JsonElement value1 = merged.get(key);
                if (value1.isJsonObject() && value2.isJsonObject()) {
                    // recursively merge
                    JsonObject childMerged = mergeJsonObject(value1.getAsJsonObject(), value2.getAsJsonObject());
                    merged.add(key, childMerged);
                } else {
                    // override

                    // TODO: maybe merge lists / append them..?
                    merged.add(key, value2);
                }
            } else {
                merged.add(key, value2);
            }
        }
        return merged;
    }
}
