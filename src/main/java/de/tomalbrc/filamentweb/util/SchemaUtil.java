package de.tomalbrc.filamentweb.util;

import com.google.gson.*;

import java.util.*;

public final class SchemaUtil {
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private SchemaUtil() {}

    public static String normalizedPath(String prefix, String name) {
        if (prefix == null || prefix.isEmpty()) return name;
        return prefix + "[" + name + "]";
    }

    private static String fieldNameFromPath(String path) {
        if (path == null || path.isBlank()) return "";
        int dot = path.lastIndexOf('.');
        int bracket = path.lastIndexOf('[');
        int start = Math.max(dot, bracket);
        String name = start >= 0 ? path.substring(start + 1) : path;
        if (name.endsWith("]")) name = name.substring(0, name.length() - 1);
        return name;
    }

    public static JsonObject resolveRef(JsonObject node, JsonObject rootSchema) {
        if (node == null || !node.has("$ref") || rootSchema == null) return node;

        String ref = node.get("$ref").getAsString();
        if (!ref.startsWith("#/")) return node;

        String[] parts = ref.substring(2).split("/");
        JsonElement current = rootSchema;

        for (String part : parts) {
            String key = part.replace("~1", "/").replace("~0", "~");
            if (current instanceof JsonObject obj) {
                if (obj.has(key)) current = obj.get(key);
                else if (obj.has("$defs") && obj.getAsJsonObject("$defs").has(key)) current = obj.getAsJsonObject("$defs").get(key);
                else if (obj.has("definitions") && obj.getAsJsonObject("definitions").has(key)) current = obj.getAsJsonObject("definitions").get(key);
                else return node;
            } else {
                return node;
            }
        }
        return current.isJsonObject() ? current.getAsJsonObject() : node;
    }

    public static List<String> schemaTypes(JsonObject schema) {
        if (schema == null || !schema.has("type")) return Collections.emptyList();
        JsonElement typeEl = schema.get("type");
        if (typeEl.isJsonPrimitive()) return List.of(typeEl.getAsString());
        if (typeEl.isJsonArray()) {
            List<String> types = new ArrayList<>();
            typeEl.getAsJsonArray().forEach(e -> { if(e.isJsonPrimitive()) types.add(e.getAsString()); });
            return types;
        }
        return Collections.emptyList();
    }

    public static JsonElement createDefaultForSchema(JsonObject schemaNode, JsonObject rootSchema) {
        if (schemaNode == null) return JsonNull.INSTANCE;
        JsonObject resolved = resolveRef(schemaNode, rootSchema);
        if (resolved == null) resolved = schemaNode;

        if (resolved.has("default") && !resolved.get("default").isJsonNull()) {
            return resolved.get("default");
        }

        if (resolved.has("const") && !resolved.get("const").isJsonNull()) {
            return resolved.get("const");
        }

        if (resolved.has("enum") && resolved.get("enum").isJsonArray()) {
            JsonArray arr = resolved.getAsJsonArray("enum");
            if (!arr.isEmpty()) return arr.get(0);
        }

        String type = inferType(resolved);

        if (type == null) {
            if (resolved.has("properties")) type = "object";
            else if (resolved.has("items") || resolved.has("prefixItems")) type = "array";
        }

        if (type == null) return JsonNull.INSTANCE;

        return switch (type) {
            case "boolean" -> new JsonPrimitive(false);
            case "integer", "number" -> new JsonPrimitive(0);
            case "string" -> new JsonPrimitive("");
            case "object" -> createObjectDefault(resolved, rootSchema);
            case "array" -> createArrayDefault(resolved, rootSchema);
            default -> JsonNull.INSTANCE;
        };
    }

    private static JsonElement createObjectDefault(JsonObject schema, JsonObject rootSchema) {
        JsonObject result = new JsonObject();

        if (schema.has("properties") && schema.get("properties").isJsonObject()) {
            JsonObject props = schema.getAsJsonObject("properties");
            for (Map.Entry<String, JsonElement> entry : props.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;

                // TODO: handle "required" properly instead of this hard coded check
                JsonElement child = "components".equals(entry.getKey()) || "behaviour".equals(entry.getKey()) ? new JsonObject() : createDefaultForSchema(entry.getValue().getAsJsonObject(), rootSchema);
                if (child != null && !child.isJsonNull()) {
                    result.add(entry.getKey(), child);
                }
            }
        }

        return result;
    }

    private static JsonElement createArrayDefault(JsonObject schema, JsonObject rootSchema) {
        JsonArray result = new JsonArray();

        if (schema.has("prefixItems") && schema.get("prefixItems").isJsonArray()) {
            JsonArray prefixItems = schema.getAsJsonArray("prefixItems");
            for (JsonElement itemSchemaEl : prefixItems) {
                if (itemSchemaEl.isJsonObject()) {
                    result.add(createDefaultForSchema(itemSchemaEl.getAsJsonObject(), rootSchema));
                } else {
                    result.add(JsonNull.INSTANCE);
                }
            }
            return result;
        }

        if (schema.has("items") && schema.get("items").isJsonArray()) {
            JsonArray tupleItems = schema.getAsJsonArray("items");
            for (JsonElement itemSchemaEl : tupleItems) {
                if (itemSchemaEl.isJsonObject()) {
                    result.add(createDefaultForSchema(itemSchemaEl.getAsJsonObject(), rootSchema));
                } else {
                    result.add(JsonNull.INSTANCE);
                }
            }
            return result;
        }

        int count = 0;
        if (schema.has("minItems")) {
            count = Math.max(0, schema.get("minItems").getAsInt());
        }

        if (schema.has("items") && schema.get("items").isJsonObject() && count > 0) {
            JsonObject itemSchema = schema.getAsJsonObject("items");
            for (int i = 0; i < count; i++) {
                result.add(createDefaultForSchema(itemSchema, rootSchema));
            }
        }

        return result;
    }

    private static String inferType(JsonObject schema) {
        if (!schema.has("type")) return null;

        JsonElement typeEl = schema.get("type");
        if (typeEl.isJsonPrimitive()) {
            return typeEl.getAsString();
        }

        // if type is ["null", "string"], etc., pick the first usable one
        if (typeEl.isJsonArray()) {
            for (JsonElement el : typeEl.getAsJsonArray()) {
                if (el.isJsonPrimitive()) {
                    String t = el.getAsString();
                    if (!"null".equals(t)) return t;
                }
            }
        }

        return null;
    }

    private static List<String> tokenizePath(String path) {
        List<String> tokens = new ArrayList<>();
        if (path == null) return tokens;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); ++i) {
            char c = path.charAt(i);
            if (c == '[') {
                if (!sb.isEmpty()) { tokens.add(sb.toString()); sb.setLength(0); }
                int j = path.indexOf(']', i);
                if (j == -1) break;
                String inner = path.substring(i + 1, j);
                tokens.add(inner);
                i = j;
            } else {
                sb.append(c);
            }
        }

        if (!sb.isEmpty())
            tokens.add(sb.toString());

        return tokens;
    }

    public static JsonPrimitive coercePrimitive(String s) {
        if (s == null) return new JsonPrimitive("");
        String t = s.trim();
        if (t.equalsIgnoreCase("true"))
            return new JsonPrimitive(true);

        if (t.equalsIgnoreCase("false"))
            return new JsonPrimitive(false);
        try {
            if (t.matches("^-?\\d+$")) {
                long lv = Long.parseLong(t);
                return new JsonPrimitive(lv);
            }
            if (t.matches("^-?\\d+\\.\\d+$")) {
                double d = Double.parseDouble(t);
                return new JsonPrimitive(d);
            }
        } catch (NumberFormatException ignored) {

        }

        return new JsonPrimitive(s);
    }

    public static JsonObject resolveSchema(JsonObject schema, JsonObject rootSchema) {
        if (schema == null) return new JsonObject();
        JsonObject resolved = SchemaUtil.resolveRef(schema, rootSchema);
        return resolved == null ? new JsonObject() : resolved;
    }

    public static String schemaType(JsonObject schema) {
        if (schema == null || !schema.has("type")) return null;
        JsonElement typeEl = schema.get("type");
        if (typeEl.isJsonPrimitive()) return typeEl.getAsString();
        return null;
    }

    public static String schemaDisplayName(JsonObject schema, String path, String fallback) {
        if (schema != null && schema.has("title")) {
            return schema.get("title").getAsString();
        }

        String fieldName = fieldNameFromPath(path);
        if (fieldName.isBlank()) {
            return fallback;
        }

        if (fieldName.contains(":") || fieldName.contains("=")) {
            return fieldName;
        }

        String[] words = fieldName.split("_");
        StringBuilder displayName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                displayName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return displayName.toString().trim();
    }

    public static boolean isNestedSchemaBlock(JsonObject schema) {
        return hasComposableSchema(schema) || isObjectSchema(schema) || isArraySchema(schema);
    }

    public static JsonObject normalizeSchema(JsonObject schema, JsonObject rootSchema) {
        if (schema == null) return new JsonObject();
        JsonObject resolved = resolveRef(schema, rootSchema);
        if (resolved.has("allOf")) return mergeAllOf(resolved, rootSchema);

        return resolved;
    }

    public static boolean isObjectSchema(JsonObject schema) {
        String type = schemaType(schema);
        return "object".equals(type) || (schema != null && (schema.has("properties") || schema.has("additionalProperties")));
    }

    public static boolean isArraySchema(JsonObject schema) {
        String type = schemaType(schema);
        return "array".equals(type) || (schema != null && schema.has("items"));
    }

    public static boolean hasComposableSchema(JsonObject schema) {
        return schema != null && (schema.has("anyOf") || schema.has("oneOf") || schema.has("allOf") || (schema.has("type") && schema.get("type").isJsonArray()));
    }

    public static String schemaLabel(JsonObject schema, int index) {
        if (schema != null && schema.has("title")) return schema.get("title").getAsString();
        if (schema != null && schema.has("const") && schema.get("const").isJsonPrimitive()) return schema.get("const").getAsString();
        String type = schemaType(schema);
        if (type != null) {
            if ("object".equals(type) && schema.has("additionalProperties") && !schema.has("properties")) return "Map";
            if ("array".equals(type)) return "List";
            return Character.toUpperCase(type.charAt(0)) + type.substring(1);
        }
        return "Option " + (index + 1);
    }

    public static JsonObject mergeAllOf(JsonObject schema, JsonObject rootSchema) {
        JsonObject merged = new JsonObject();

        if (schema.has("title")) merged.addProperty("title", schema.get("title").getAsString());
        if (schema.has("description")) merged.addProperty("description", schema.get("description").getAsString());

        JsonObject properties = new JsonObject();
        Set<String> required = new LinkedHashSet<>();
        JsonElement additionalProperties = null;
        JsonElement items = null;
        String type = schemaType(schema);

        if (schema.has("allOf") && schema.get("allOf").isJsonArray()) {
            for (JsonElement branchEl : schema.getAsJsonArray("allOf")) {
                if (!branchEl.isJsonObject()) continue;
                JsonObject branch = normalizeSchema(resolveSchema(branchEl.getAsJsonObject(), rootSchema), rootSchema);

                if (branch.has("type") && type == null && branch.get("type").isJsonPrimitive()) {
                    type = branch.get("type").getAsString();
                }

                if (branch.has("properties") && branch.get("properties").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : branch.getAsJsonObject("properties").entrySet()) {
                        properties.add(entry.getKey(), entry.getValue());
                    }
                }

                if (branch.has("required") && branch.get("required").isJsonArray()) {
                    for (JsonElement e : branch.getAsJsonArray("required")) {
                        required.add(e.getAsString());
                    }
                }

                if (branch.has("additionalProperties")) additionalProperties = branch.get("additionalProperties");
                if (branch.has("items")) items = branch.get("items");

                if (branch.has("enum")) merged.add("enum", branch.get("enum"));
                if (branch.has("const")) merged.add("const", branch.get("const"));
            }
        }

        if (type != null) merged.addProperty("type", type);
        if (!properties.isEmpty()) merged.add("properties", properties);
        if (!required.isEmpty()) {
            JsonArray req = new JsonArray();
            required.forEach(req::add);
            merged.add("required", req);
        }
        if (additionalProperties != null) merged.add("additionalProperties", additionalProperties);
        if (items != null) merged.add("items", items);

        return merged;
    }

    public static List<JsonObject> extractBranches(JsonObject schema, JsonObject rootSchema) {
        List<JsonObject> branches = new ArrayList<>();
        if (schema == null) return branches;

        if (schema.has("allOf") && schema.get("allOf").isJsonArray()) {
            branches.add(mergeAllOf(schema, rootSchema));
            return branches;
        }

        if (schema.has("anyOf") && schema.get("anyOf").isJsonArray()) {
            for (JsonElement e : schema.getAsJsonArray("anyOf")) {
                if (e.isJsonObject()) branches.add(normalizeSchema(resolveSchema(e.getAsJsonObject(), rootSchema), rootSchema));
            }
            return branches;
        }

        if (schema.has("oneOf") && schema.get("oneOf").isJsonArray()) {
            for (JsonElement e : schema.getAsJsonArray("oneOf")) {
                if (e.isJsonObject()) branches.add(normalizeSchema(resolveSchema(e.getAsJsonObject(), rootSchema), rootSchema));
            }
            return branches;
        }

        if (schema.has("type") && schema.get("type").isJsonArray()) {
            for (String type : schemaTypes(schema)) {
                JsonObject branch = new JsonObject();
                branch.addProperty("type", type);
                if (schema.has("title")) branch.addProperty("title", schema.get("title").getAsString());
                if (schema.has("description")) branch.addProperty("description", schema.get("description").getAsString());
                if ("object".equals(type)) {
                    if (schema.has("properties")) branch.add("properties", schema.get("properties"));
                    if (schema.has("required")) branch.add("required", schema.get("required"));
                    if (schema.has("additionalProperties")) branch.add("additionalProperties", schema.get("additionalProperties"));
                }
                if ("array".equals(type) && schema.has("items")) {
                    branch.add("items", schema.get("items"));
                }
                branches.add(normalizeSchema(branch, rootSchema));
            }
            return branches;
        }

        branches.add(normalizeSchema(schema, rootSchema));
        return branches;
    }

    public static boolean matchesSchema(JsonElement value, JsonObject schema, JsonObject rootSchema) {
        if (schema == null) return true;
        if (value == null || value.isJsonNull()) {
            List<String> types = schemaTypes(schema);
            return types.contains("null") || (types.isEmpty() && !schema.has("type"));
        }

        schema = normalizeSchema(schema, rootSchema);

        if (schema.has("const")) {
            return schema.get("const").equals(value);
        }
        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            boolean found = false;
            for (JsonElement e : schema.getAsJsonArray("enum")) {
                if (e.equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        List<String> types = schemaTypes(schema);
        if (!types.isEmpty()) {
            boolean typeMatches = false;
            for (String type : types) {
                if (checkBaseType(value, type)) {
                    typeMatches = true;
                    break;
                }
            }
            if (!typeMatches) return false;
        }

        if (value.isJsonObject()) {
            return validateObject(value.getAsJsonObject(), schema, rootSchema);
        } else if (value.isJsonArray()) {
            return validateArray(value.getAsJsonArray(), schema, rootSchema);
        }

        return true;
    }

    private static boolean checkBaseType(JsonElement value, String type) {
        return switch (type) {
            case "string" -> value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
            case "number", "integer" -> value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
            case "boolean" -> value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
            case "object" -> value.isJsonObject();
            case "array" -> value.isJsonArray();
            case "null" -> value.isJsonNull();
            default -> true;
        };
    }

    private static boolean validateObject(JsonObject obj, JsonObject schema, JsonObject rootSchema) {
        // Check Required fields
        if (schema.has("required") && schema.get("required").isJsonArray()) {
            for (JsonElement req : schema.getAsJsonArray("required")) {
                if (!obj.has(req.getAsString())) return false;
            }
        }

        // Check Properties
        if (schema.has("properties") && schema.get("properties").isJsonObject()) {
            JsonObject props = schema.getAsJsonObject("properties");
            for (Map.Entry<String, JsonElement> entry : props.entrySet()) {
                if (obj.has(entry.getKey())) {
                    if (!matchesSchema(obj.get(entry.getKey()), entry.getValue().getAsJsonObject(), rootSchema)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean validateArray(JsonArray array, JsonObject schema, JsonObject rootSchema) {
        if (schema.has("items") && schema.get("items").isJsonObject()) {
            JsonObject itemSchema = schema.getAsJsonObject("items");
            for (JsonElement item : array) {
                if (!matchesSchema(item, itemSchema, rootSchema)) return false;
            }
        }
        return true;
    }

    public static int pickBranchIndex(JsonElement value, List<JsonObject> branches, JsonObject rootSchema) {
        for (int i = 0; i < branches.size(); i++) {
            if (matchesSchema(value, branches.get(i), rootSchema)) return i;
        }
        return 0;
    }
}