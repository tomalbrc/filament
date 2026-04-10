package de.tomalbrc.filamentweb.util;

import com.google.gson.*;

import java.math.BigDecimal;
import java.math.BigInteger;
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
        int start = Math.max(path.lastIndexOf('.'), path.lastIndexOf('['));
        String name = start >= 0 ? path.substring(start + 1) : path;
        return name.endsWith("]") ? name.substring(0, name.length() - 1) : name;
    }

    public static JsonObject resolveRef(JsonObject node, JsonObject rootSchema) {
        if (node == null || !node.has("$ref") || rootSchema == null) return node;

        String ref = node.get("$ref").getAsString();
        if (!ref.startsWith("#/")) return node;

        JsonElement current = rootSchema;
        for (String part : ref.substring(2).split("/")) {
            String key = part.replace("~1", "/").replace("~0", "~");
            if (current instanceof JsonObject obj) {
                if (obj.has(key)) current = obj.get(key);
                else if (hasKeyInObject(obj, "$defs", key)) current = obj.getAsJsonObject("$defs").get(key);
                else if (hasKeyInObject(obj, "definitions", key)) current = obj.getAsJsonObject("definitions").get(key);
                else return node;
            } else {
                return node;
            }
        }
        return current.isJsonObject() ? current.getAsJsonObject() : node;
    }

    private static boolean hasKeyInObject(JsonObject obj, String parentKey, String childKey) {
        return obj.has(parentKey) && obj.getAsJsonObject(parentKey).has(childKey);
    }

    private static List<String> schemaTypes(JsonObject schema) {
        if (schema == null || !schema.has("type")) return Collections.emptyList();
        JsonElement typeEl = schema.get("type");
        if (typeEl.isJsonPrimitive()) return List.of(typeEl.getAsString());
        if (typeEl.isJsonArray()) {
            List<String> types = new ArrayList<>();
            typeEl.getAsJsonArray().forEach(e -> { if (e.isJsonPrimitive()) types.add(e.getAsString()); });
            return types;
        }
        return Collections.emptyList();
    }

    public static JsonElement createDefaultForSchema(JsonObject schemaNode, JsonObject rootSchema) {
        if (schemaNode == null) return JsonNull.INSTANCE;
        JsonObject resolved = resolveRef(schemaNode, rootSchema);
        if (resolved == null) resolved = schemaNode;

        if (resolved.has("default") && !resolved.get("default").isJsonNull()) return resolved.get("default");
        if (resolved.has("const") && !resolved.get("const").isJsonNull()) return resolved.get("const");
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
            List<String> required = getRequiredList(schema);
            for (Map.Entry<String, JsonElement> entry : props.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                JsonElement child = createDefaultForSchema(entry.getValue().getAsJsonObject(), rootSchema);
                if (child != null && !child.isJsonNull() && required.contains(entry.getKey())) {
                    result.add(entry.getKey(), child);
                }
            }
        }
        return result;
    }

    private static List<String> getRequiredList(JsonObject obj) {
        if (!obj.has("required") || !obj.get("required").isJsonArray()) return Collections.emptyList();
        return obj.getAsJsonArray("required").asList().stream().map(JsonElement::getAsString).toList();
    }

    private static JsonElement createArrayDefault(JsonObject schema, JsonObject rootSchema) {
        if (schema.has("prefixItems") && schema.get("prefixItems").isJsonArray()) {
            return mapDefaults(schema.getAsJsonArray("prefixItems"), rootSchema);
        }
        if (schema.has("items") && schema.get("items").isJsonArray()) {
            return mapDefaults(schema.getAsJsonArray("items"), rootSchema);
        }
        JsonArray result = new JsonArray();
        int count = schema.has("minItems") ? Math.max(0, schema.get("minItems").getAsInt()) : 0;
        if (count > 0 && schema.has("items") && schema.get("items").isJsonObject()) {
            JsonObject itemSchema = schema.getAsJsonObject("items");
            for (int i = 0; i < count; i++) result.add(createDefaultForSchema(itemSchema, rootSchema));
        }
        return result;
    }

    private static JsonArray mapDefaults(JsonArray arr, JsonObject rootSchema) {
        JsonArray res = new JsonArray();
        arr.forEach(e -> res.add(e.isJsonObject() ? createDefaultForSchema(e.getAsJsonObject(), rootSchema) : JsonNull.INSTANCE));
        return res;
    }

    private static String inferType(JsonObject schema) {
        List<String> types = schemaTypes(schema);
        for (String t : types) {
            if (!"null".equals(t)) return t;
        }
        return null;
    }

    public static JsonObject resolveSchema(JsonObject schema, JsonObject rootSchema) {
        if (schema == null) return new JsonObject();
        JsonObject resolved = SchemaUtil.resolveRef(schema, rootSchema);
        return resolved == null ? new JsonObject() : resolved;
    }

    public static String schemaType(JsonObject schema) {
        if (schema == null || !schema.has("type")) return null;
        JsonElement typeEl = schema.get("type");
        return typeEl.isJsonPrimitive() ? typeEl.getAsString() : null;
    }

    public static String schemaDisplayName(JsonObject schema, String path, String fallback) {
        if (schema != null && schema.has("title")) return schema.get("title").getAsString();
        String fieldName = fieldNameFromPath(path);
        if (fieldName.isBlank()) return fallback;
        if (fieldName.contains(":") || fieldName.contains("=")) return fieldName;

        StringBuilder displayName = new StringBuilder();
        for (String word : fieldName.split("_")) {
            if (!word.isEmpty()) {
                displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
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
        return resolved.has("allOf") ? mergeAllOf(resolved, rootSchema) : resolved;
    }

    public static boolean isObjectSchema(JsonObject schema) {
        return "object".equals(schemaType(schema)) || (schema != null && (schema.has("properties") || schema.has("additionalProperties")));
    }

    public static boolean isArraySchema(JsonObject schema) {
        return "array".equals(schemaType(schema)) || (schema != null && schema.has("items"));
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

    private static JsonObject mergeAllOf(JsonObject schema, JsonObject rootSchema) {
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

                if (type == null && branch.has("type") && branch.get("type").isJsonPrimitive()) type = branch.get("type").getAsString();
                if (branch.has("properties") && branch.get("properties").isJsonObject()) {
                    branch.getAsJsonObject("properties").entrySet().forEach(e -> properties.add(e.getKey(), e.getValue()));
                }
                addRequired(branch, required);
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
        if (schema == null) return new ArrayList<>();

        if (schema.has("allOf") && schema.get("allOf").isJsonArray()) {
            return List.of(mergeAllOf(schema, rootSchema));
        }
        if (schema.has("anyOf") && schema.get("anyOf").isJsonArray()) return extractOfArray(schema.getAsJsonArray("anyOf"), rootSchema);
        if (schema.has("oneOf") && schema.get("oneOf").isJsonArray()) return extractOfArray(schema.getAsJsonArray("oneOf"), rootSchema);

        List<JsonObject> branches = new ArrayList<>();
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
                if ("array".equals(type) && schema.has("items")) branch.add("items", schema.get("items"));
                branches.add(normalizeSchema(branch, rootSchema));
            }
            return branches;
        }

        branches.add(normalizeSchema(schema, rootSchema));
        return branches;
    }

    private static List<JsonObject> extractOfArray(JsonArray arr, JsonObject rootSchema) {
        List<JsonObject> res = new ArrayList<>();
        arr.forEach(e -> { if (e.isJsonObject()) res.add(normalizeSchema(resolveSchema(e.getAsJsonObject(), rootSchema), rootSchema)); });
        return res;
    }

    private static boolean matchesSchema(JsonElement value, JsonObject schema, JsonObject rootSchema) {
        if (schema == null) return true;
        if (value == null || value.isJsonNull()) {
            List<String> types = schemaTypes(schema);
            return types.contains("null") || (types.isEmpty() && !schema.has("type"));
        }

        schema = normalizeSchema(schema, rootSchema);

        if (schema.has("const")) return schema.get("const").equals(value);
        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            for (JsonElement e : schema.getAsJsonArray("enum")) {
                if (e.equals(value)) return true;
            }
            return false;
        }

        List<String> types = schemaTypes(schema);
        if (!types.isEmpty() && types.stream().noneMatch(t -> checkBaseType(value, t))) return false;

        if (value.isJsonObject()) return validateObject(value.getAsJsonObject(), schema, rootSchema);
        if (value.isJsonArray()) return validateArray(value.getAsJsonArray(), schema, rootSchema);

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
        List<String> required = getRequiredList(schema);
        for (String req : required) {
            if (!obj.has(req)) return false;
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (required.contains(entry.getKey()) && !validateObjectProperty(entry.getKey(), entry.getValue(), schema, rootSchema)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateObjectProperty(String key, JsonElement value, JsonObject schema, JsonObject rootSchema) {
        if (schema.has("properties") && schema.get("properties").isJsonObject()) {
            JsonObject props = schema.getAsJsonObject("properties");
            if (props.has(key)) {
                JsonElement propSchema = props.get(key);
                return propSchema.isJsonObject() && matchesSchema(value, propSchema.getAsJsonObject(), rootSchema);
            }
        }

        if (schema.has("patternProperties") && schema.get("patternProperties").isJsonObject()) {
            JsonObject patterns = schema.getAsJsonObject("patternProperties");
            for (Map.Entry<String, JsonElement> entry : patterns.entrySet()) {
                if (entry.getValue().isJsonObject() && key.matches(entry.getKey())) {
                    return matchesSchema(value, entry.getValue().getAsJsonObject(), rootSchema);
                }
            }
        }

        if (schema.has("additionalProperties")) {
            JsonElement additional = schema.get("additionalProperties");
            if (additional.isJsonPrimitive() && additional.getAsJsonPrimitive().isBoolean()) return additional.getAsBoolean();
            if (additional.isJsonObject()) return matchesSchema(value, additional.getAsJsonObject(), rootSchema);
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

    public static JsonObject resolveObjectMemberSchema(JsonObject node, String key, JsonObject rootSchema) {
        if (node == null || key == null) return null;

        JsonObject normalized = SchemaUtil.normalizeSchema(node, rootSchema);

        if (normalized.has("properties") && normalized.get("properties").isJsonObject()) {
            JsonObject props = normalized.getAsJsonObject("properties");
            if (props.has(key) && props.get(key).isJsonObject()) {
                return resolveSchema(props.getAsJsonObject(key), rootSchema);
            }
        }

        if (normalized.has("patternProperties") && normalized.get("patternProperties").isJsonObject()) {
            JsonObject patterns = normalized.getAsJsonObject("patternProperties");
            for (Map.Entry<String, JsonElement> entry : patterns.entrySet()) {
                if (entry.getValue().isJsonObject() && key.matches(entry.getKey())) {
                    return resolveSchema(entry.getValue().getAsJsonObject(), rootSchema);
                }
            }
        }

        int index = parseSafeInt(key, -1);
        if (index >= 0 && isArraySchema(normalized)) {
            JsonObject arrayMember = resolveArrayMemberSchema(normalized, index, rootSchema);
            if (arrayMember != null) return arrayMember;
        }

        if (normalized.has("additionalProperties")) {
            JsonElement additional = normalized.get("additionalProperties");
            if (additional.isJsonObject()) return resolveSchema(additional.getAsJsonObject(), rootSchema);
            if (additional.isJsonPrimitive() && additional.getAsJsonPrimitive().isBoolean()) {
                return additional.getAsBoolean() ? new JsonObject() : null;
            }
        }

        return null;
    }

    private static JsonObject resolveArrayMemberSchema(JsonObject node, int index, JsonObject rootSchema) {
        if (node == null) return null;

        if (node.has("prefixItems") && node.get("prefixItems").isJsonArray()) {
            JsonArray prefixItems = node.getAsJsonArray("prefixItems");
            if (index >= 0 && index < prefixItems.size()) {
                JsonElement item = prefixItems.get(index);
                JsonObject resolved = resolveSchemaElement(item, rootSchema);
                if (resolved != null) return resolved;
            }
        }

        if (node.has("items")) {
            JsonElement items = node.get("items");

            if (items.isJsonObject() || (items.isJsonPrimitive() && items.getAsJsonPrimitive().isBoolean())) {
                return resolveSchemaElement(items, rootSchema);
            }

            if (items.isJsonArray()) {
                JsonArray tupleItems = items.getAsJsonArray();
                if (index >= 0 && index < tupleItems.size()) {
                    JsonObject resolved = resolveSchemaElement(tupleItems.get(index), rootSchema);
                    if (resolved != null) return resolved;
                }
            }
        }

        if (node.has("additionalItems")) {
            JsonElement additionalItems = node.get("additionalItems");
            if (additionalItems.isJsonObject() || (additionalItems.isJsonPrimitive() && additionalItems.getAsJsonPrimitive().isBoolean())) {
                return resolveSchemaElement(additionalItems, rootSchema);
            }
        }

        return null;
    }

    private static JsonObject resolveSchemaElement(JsonElement schemaEl, JsonObject rootSchema) {
        if (schemaEl == null || schemaEl.isJsonNull()) return null;

        if (schemaEl.isJsonObject()) {
            return resolveSchema(schemaEl.getAsJsonObject(), rootSchema);
        }

        if (schemaEl.isJsonPrimitive() && schemaEl.getAsJsonPrimitive().isBoolean()) {
            return schemaEl.getAsBoolean() ? new JsonObject() : null;
        }

        return null;
    }

    public static JsonObject defaultEntrySchema(JsonObject node, JsonObject rootSchema) {
        if (node == null) return null;
        if (node.has("patternProperties") && node.get("patternProperties").isJsonObject()) {
            JsonObject patterns = node.getAsJsonObject("patternProperties");
            if (patterns.has("^.*$") && patterns.get("^.*$").isJsonObject()) return resolveSchema(patterns.getAsJsonObject("^.*$"), rootSchema);
            for (Map.Entry<String, JsonElement> entry : patterns.entrySet()) {
                if (entry.getValue().isJsonObject()) return resolveSchema(entry.getValue().getAsJsonObject(), rootSchema);
            }
        }
        if (node.has("additionalProperties") && node.get("additionalProperties").isJsonObject()) {
            return resolveSchema(node.getAsJsonObject("additionalProperties"), rootSchema);
        }
        return null;
    }

    private static void mergeRequired(JsonObject target, JsonObject source) {
        Set<String> required = new LinkedHashSet<>();
        addRequired(target, required);
        addRequired(source, required);
        if (!required.isEmpty()) {
            JsonArray arr = new JsonArray();
            required.forEach(arr::add);
            target.add("required", arr);
        }
    }

    private static void addRequired(JsonObject obj, Set<String> set) {
        if (obj.has("required") && obj.get("required").isJsonArray()) {
            obj.getAsJsonArray("required").forEach(e -> set.add(e.getAsString()));
        }
    }

    public static JsonObject mergeSchemaObjects(JsonObject base, JsonObject overlay) {
        JsonObject result = deepCopy(base);
        for (Map.Entry<String, JsonElement> entry : overlay.entrySet()) {
            String key = entry.getKey();
            if ("properties".equals(key) && entry.getValue().isJsonObject()) {
                JsonObject mergedProps = result.has("properties") && result.get("properties").isJsonObject() ? result.getAsJsonObject("properties") : new JsonObject();
                entry.getValue().getAsJsonObject().entrySet().forEach(prop -> mergedProps.add(prop.getKey(), copyElement(prop.getValue())));
                result.add("properties", mergedProps);
                continue;
            }
            if ("required".equals(key) && entry.getValue().isJsonArray()) {
                mergeRequired(result, overlay);
                continue;
            }
            if ("anyOf".equals(key) || "oneOf".equals(key) || "allOf".equals(key)) continue;
            result.add(key, copyElement(entry.getValue()));
        }
        mergeRequired(result, overlay);
        return result;
    }

    public static JsonObject effectiveSchemaAtPath(JsonObject rootSchema, JsonElement documentJson, String path) {
        if (rootSchema == null) return new JsonObject();
        if (path == null || path.isBlank()) return SchemaUtil.normalizeSchema(rootSchema, rootSchema);

        List<String> segments = JsonPathUtil.parsePath(path);
        JsonObject currentSchema = SchemaUtil.normalizeSchema(rootSchema, rootSchema);
        JsonElement currentValue = documentJson;

        for (String segment : segments) {
            JsonObject resolvedParent = resolveNodeSchema(currentSchema, currentValue, rootSchema);
            if (resolvedParent == null) return new JsonObject();

            currentSchema = SchemaUtil.resolveObjectMemberSchema(resolvedParent, segment, rootSchema);
            if (currentSchema == null) return new JsonObject();

            currentValue = childValue(currentValue, segment);
        }

        return SchemaUtil.normalizeSchema(currentSchema, rootSchema);
    }

    private static JsonObject resolveNodeSchema(JsonObject currentSchema, JsonElement currentValue, JsonObject rootSchema) {
        if (currentSchema == null) return null;
        JsonObject normalized = SchemaUtil.normalizeSchema(currentSchema, rootSchema);
        if (normalized.has("anyOf") || normalized.has("oneOf")) {
            List<JsonObject> branches = SchemaUtil.extractBranches(normalized, rootSchema);
            if (!branches.isEmpty()) {
                int selected = SchemaUtil.pickBranchIndex(currentValue, branches, rootSchema);
                if (selected < 0 || selected >= branches.size()) selected = 0;
                return resolveNodeSchema(branches.get(selected), currentValue, rootSchema);
            }
        }
        return normalized;
    }

    public static JsonElement coerceSubmittedValue(JsonElement submitted, JsonObject schema, JsonElement currentValue) {
        if (submitted == null || submitted.isJsonNull()) return JsonNull.INSTANCE;
        if (schema == null) schema = new JsonObject();

        String type = schemaType(schema);
        if (type == null && currentValue != null && !currentValue.isJsonNull()) type = SchemaUtil.inferType(currentValue);

        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            JsonElement coercedEnumValue = coerceByType(submitted, type, currentValue);
            for (JsonElement allowed : schema.getAsJsonArray("enum")) {
                if (allowed == null) continue;
                if (allowed.equals(coercedEnumValue)) return coercedEnumValue;
                if (allowed.isJsonPrimitive() && coercedEnumValue.isJsonPrimitive()) {
                    String a = allowed.getAsJsonPrimitive().isString() ? allowed.getAsString() : allowed.toString();
                    String b = coercedEnumValue.getAsJsonPrimitive().isString() ? coercedEnumValue.getAsString() : coercedEnumValue.toString();
                    if (a.equals(b)) return coercedEnumValue;
                }
            }
            return coercedEnumValue;
        }
        return coerceByType(submitted, type, currentValue);
    }

    public static JsonElement coerceByType(JsonElement submitted, String type, JsonElement currentValue) {
        if (submitted == null || submitted.isJsonNull()) return JsonNull.INSTANCE;
        if (submitted.isJsonObject() || submitted.isJsonArray()) return submitted.deepCopy();

        String raw = getRawString(submitted);
        if (raw == null) return JsonNull.INSTANCE;
        raw = raw.trim();

        if (type == null || type.isEmpty()) {
            type = inferType(currentValue);
        }

        if (raw.isEmpty()) return "string".equals(type) ? new JsonPrimitive("") : JsonNull.INSTANCE;

        return switch (type) {
            case "boolean" -> new JsonPrimitive(parseBoolean(raw));
            case "integer" -> new JsonPrimitive(parseInteger(raw));
            case "number" -> new JsonPrimitive(parseNumber(raw));
            case "string", "object", "array" -> new JsonPrimitive(raw);
            default -> autoDetect(raw);
        };
    }

    private static String getRawString(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive p = element.getAsJsonPrimitive();
            if (p.isBoolean()) return Boolean.toString(p.getAsBoolean());
            if (p.isNumber()) return p.getAsNumber().toString();
            return p.getAsString();
        }
        return element.toString();
    }

    // goofy
    private static JsonElement autoDetect(String raw) {
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) return new JsonPrimitive(Boolean.parseBoolean(raw));
        try {
            if (raw.matches("[-+]?\\d+")) return new JsonPrimitive(parseInteger(raw));
            if (raw.matches("[-+]?((\\d*\\.\\d+)|(\\d+\\.\\d*))(?:[eE][-+]?\\d+)?") || raw.matches("[-+]?\\d+[eE][-+]?\\d+")) return new JsonPrimitive(parseNumber(raw));
        } catch (Exception ignored) {}

        return new JsonPrimitive(raw);
    }

    private static JsonElement childValue(JsonElement currentValue, String segment) {
        if (currentValue == null || currentValue.isJsonNull()) return JsonNull.INSTANCE;
        if (currentValue.isJsonObject()) return currentValue.getAsJsonObject().has(segment) ? currentValue.getAsJsonObject().get(segment) : JsonNull.INSTANCE;
        if (currentValue.isJsonArray() && JsonPathUtil.isInteger(segment)) {
            JsonArray arr = currentValue.getAsJsonArray();
            int idx = SchemaUtil.parseSafeInt(segment, -1);
            return idx >= 0 && idx < arr.size() ? arr.get(idx) : JsonNull.INSTANCE;
        }
        return JsonNull.INSTANCE;
    }

    private static JsonElement copyElement(JsonElement element) {
        return JsonParser.parseString(element.toString());
    }

    public static JsonObject deepCopy(JsonObject obj) {
        return JsonParser.parseString(obj.toString()).getAsJsonObject();
    }

    public static boolean hasAllOf(JsonObject schema) {
        return schema != null && schema.has("allOf") && schema.get("allOf").isJsonArray();
    }

    public static boolean hasAnyOfOrOneOf(JsonObject schema) {
        return schema != null && (schema.has("anyOf") || schema.has("oneOf"));
    }

    private static String inferType(JsonElement value) {
        if (value == null || value.isJsonNull()) return "string";
        if (value.isJsonObject()) return "object";
        if (value.isJsonArray()) return "array";
        if (!value.isJsonPrimitive()) return "string";

        JsonPrimitive p = value.getAsJsonPrimitive();
        if (p.isBoolean()) return "boolean";
        if (p.isNumber()) {
            String s = p.getAsNumber().toString();
            return (s.contains(".") || s.contains("e") || s.contains("E")) ? "number" : "integer";
        }
        return "string";
    }

    private static boolean parseBoolean(String raw) {
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("on");
    }

    private static Number parseInteger(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return new BigInteger(raw.trim());
        }
    }

    private static Number parseNumber(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return new BigDecimal(raw.trim());
        }
    }

    public static int parseSafeInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static Integer getMaxItems(JsonObject schema) {
        if (schema == null || !schema.has("maxItems")) return null;
        JsonElement el = schema.get("maxItems");
        if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isNumber()) return null;

        try {
            return el.getAsInt();
        } catch (Exception ignored) {
            return null;
        }
    }
}