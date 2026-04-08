package de.tomalbrc.filamentweb.service;

import com.google.gson.*;
import de.tomalbrc.filamentweb.SchemaFormBuilder;
import de.tomalbrc.filamentweb.asset.Asset;
import de.tomalbrc.filamentweb.asset.AssetStore;
import de.tomalbrc.filamentweb.util.JsonPathUtil;
import de.tomalbrc.filamentweb.util.SchemaUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FragmentServlet extends HttpServlet {
    public enum Operation {
        UPDATE_FIELD("updateField"),
        UPDATE_CHOICE("updateChoice"),
        ADD_ARRAY("addArray"),
        RENAME_OBJECT("renameObject"),
        ADD_OBJECT("addObject"),
        REMOVE_OBJECT("removeObject");

        final String op;
        static final Map<String, Operation> map = new HashMap<>();

        Operation(String op) {
            this.op = op;
        }

        public static Operation fromString(String op) {
            return map.get(op);
        }

        public String toString() {
            return this.op;
        }

        static {
            for (Operation operation : values()) {
                map.put(operation.op, operation);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");

        String uuid = req.getParameter("name");
        Operation op = Operation.fromString(req.getParameter("op"));
        String path = req.getParameter("path");
        String key = req.getParameter("key");
        String value = req.getParameter("value");
        String choice = req.getParameter("value");

        if (uuid == null || op == null || path == null) {
            resp.setStatus(400);
            resp.getWriter().write("Missing parameters");
            return;
        }

        Asset asset;
        try {
            asset = AssetStore.getAsset(UUID.fromString(uuid));
        } catch (Exception e) {
            asset = null;
        }

        if (asset == null) {
            resp.setStatus(404);
            resp.getWriter().write("Asset not found");
            return;
        }

        JsonObject schemaRoot = asset.schema != null && asset.schema.isJsonObject()
                ? asset.schema.getAsJsonObject()
                : new JsonObject();

        JsonElement documentJson = asset.readJson();

        switch (op) {
            case UPDATE_FIELD: {
                JsonObject fieldSchema = effectiveSchemaAtPath(schemaRoot, documentJson, path);
                JsonElement currentValue = JsonPathUtil.getElementAtPath(documentJson, path);
                String submittedRaw = extractSubmittedRawValue(req, path, value);
                JsonElement submitted = submittedRaw == null ? JsonNull.INSTANCE : new JsonPrimitive(submittedRaw);
                JsonElement coerced = coerceSubmittedValue(submitted, fieldSchema, currentValue);

                JsonPathUtil.setValueAtPath(documentJson, path, coerced);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                return;
            }

            case UPDATE_CHOICE: {
                JsonObject nodeSchema = effectiveSchemaAtPath(schemaRoot, documentJson, path);
                List<JsonObject> branches = SchemaUtil.extractBranches(nodeSchema, schemaRoot);

                if (branches.isEmpty()) {
                    resp.setStatus(400);
                    resp.getWriter().write("No schema branches available");
                    return;
                }

                int selectedIndex = parseSafeInt(req.getParameter("choice"), 0);
                if (selectedIndex < 0 || selectedIndex >= branches.size()) {
                    selectedIndex = 0;
                }

                JsonObject selectedBranch = branches.get(selectedIndex);
                JsonElement replacement = SchemaUtil.createDefaultForSchema(selectedBranch, schemaRoot);
                JsonPathUtil.setValueAtPath(documentJson, path, replacement);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderComposedFieldFragment(uuid, path, nodeSchema, replacement, schemaRoot).render());
                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                return;
            }

            case ADD_ARRAY: {
                JsonElement target = JsonPathUtil.getElementAtPath(documentJson, path);
                if (target == null || !target.isJsonArray()) {
                    resp.setStatus(400);
                    resp.getWriter().write("Target is not an array");
                    return;
                }

                JsonArray arr = target.getAsJsonArray();

                JsonObject arrSchema = effectiveSchemaAtPath(schemaRoot, documentJson, path);
                JsonObject itemsSchema = arrSchema.has("items") && arrSchema.get("items").isJsonObject()
                        ? SchemaUtil.resolveRef(arrSchema.getAsJsonObject("items"), schemaRoot)
                        : new JsonObject();

                JsonElement newItem = SchemaUtil.createDefaultForSchema(itemsSchema, schemaRoot);
                arr.add(newItem);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderArrayContainer(uuid, path, itemsSchema, arr, schemaRoot).render());
                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                return;
            }

            case RENAME_OBJECT:
            case ADD_OBJECT:
            case REMOVE_OBJECT: {
                JsonElement el = JsonPathUtil.getElementAtPath(documentJson, path);

                if (el == null || el.isJsonNull()) {
                    resp.setStatus(400);
                    resp.getWriter().write("Target not found");
                    return;
                }

                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    JsonObject objectSchema = effectiveSchemaAtPath(schemaRoot, documentJson, path);

                    if (op == Operation.ADD_OBJECT) {
                        String newKey = req.getParameter("key");
                        if (newKey == null || newKey.isBlank()) {
                            newKey = "newKey";
                        }
                        newKey = newKey.trim();

                        if (!obj.has(newKey)) {
                            JsonObject propertySchema = schemaForObjectKey(objectSchema, schemaRoot, newKey);
                            JsonElement newVal = SchemaUtil.createDefaultForSchema(propertySchema, schemaRoot);

                            // TODO: only create required fields for the "newVal" default value!
                            if ("components".equals(newKey) || "behaviour".equals(newKey)) newVal = new JsonObject();
                            obj.add(newKey, newVal);
                            asset.apply(documentJson);
                        }

                    } else if (op == Operation.REMOVE_OBJECT) {
                        if (key != null) {
                            obj.remove(key);
                            asset.apply(documentJson);
                        }
                    } else {
                        String newKey = req.getParameter("newKey");
                        if (newKey != null) newKey = newKey.trim();

                        if (key != null && newKey != null && !newKey.isBlank() && !key.equals(newKey) && obj.has(key) && !obj.has(newKey)) {
                            JsonElement moved = obj.remove(key);
                            obj.add(newKey, moved);
                            asset.apply(documentJson);
                        }
                    }

                    resp.getWriter().write(SchemaFormBuilder.renderObjectFieldsContainer(uuid, path, objectSchema, obj, schemaRoot).render());
                } else if (el.isJsonArray()) {
                    JsonArray arr = el.getAsJsonArray();

                    if (op == Operation.REMOVE_OBJECT && key != null) {
                        try {
                            int idx = Integer.parseInt(key);
                            if (idx >= 0 && idx < arr.size()) {
                                arr.remove(idx);
                                asset.apply(documentJson);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    JsonObject arrSchema = effectiveSchemaAtPath(schemaRoot, documentJson, path);
                    JsonObject itemsSchema = arrSchema.has("items") && arrSchema.get("items").isJsonObject()
                            ? SchemaUtil.resolveRef(arrSchema.getAsJsonObject("items"), schemaRoot)
                            : new JsonObject();

                    resp.getWriter().write(SchemaFormBuilder.renderArrayContainer(uuid, path, itemsSchema, arr, schemaRoot).render());
                } else {
                    resp.setStatus(400);
                    resp.getWriter().write("Unsupported target type");
                    return;
                }

                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                return;
            }

            default:
                resp.setStatus(400);
                resp.getWriter().write("Unknown operation");
        }
    }

    private static String extractSubmittedRawValue(HttpServletRequest req, String path, String fallback) {
        String[] values = req.getParameterValues(path);
        if (values != null && values.length > 0) {
            for (int i = values.length - 1; i >= 0; i--) {
                if (values[i] != null) {
                    return values[i];
                }
            }
        }
        return fallback;
    }

    private static JsonObject schemaForObjectKey(JsonObject objectSchema, JsonObject rootSchema, String key) {
        JsonObject resolved = objectSchema != null ? objectSchema : new JsonObject();

        if (resolved.has("properties") && resolved.get("properties").isJsonObject()) {
            JsonObject props = resolved.getAsJsonObject("properties");
            if (props.has(key) && props.get(key).isJsonObject()) {
                return SchemaUtil.resolveRef(props.getAsJsonObject(key), rootSchema);
            }
        }

        if (resolved.has("additionalProperties")) {
            JsonElement additional = resolved.get("additionalProperties");
            if (additional.isJsonObject()) {
                return SchemaUtil.resolveRef(additional.getAsJsonObject(), rootSchema);
            }
            if (additional.isJsonPrimitive() && additional.getAsJsonPrimitive().isBoolean() && additional.getAsBoolean()) {
                return new JsonObject();
            }
        }

        return new JsonObject();
    }

    private static JsonElement coerceSubmittedValue(JsonElement submitted, JsonObject schema, JsonElement currentValue) {
        if (submitted == null || submitted.isJsonNull()) {
            return JsonNull.INSTANCE;
        }

        if (schema == null) {
            schema = new JsonObject();
        }

        String type = null;
        if (schema.has("type")) {
            JsonElement t = schema.get("type");
            if (t.isJsonPrimitive()) {
                type = t.getAsString();
            }
        } else if (currentValue != null && !currentValue.isJsonNull()) {
            type = inferType(currentValue);
        }

        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            JsonElement coercedEnumValue = coerceByType(submitted, type, currentValue);
            for (JsonElement allowed : schema.getAsJsonArray("enum")) {
                if (allowed == null) continue;
                if (allowed.equals(coercedEnumValue)) {
                    return coercedEnumValue;
                }
                if (allowed.isJsonPrimitive() && coercedEnumValue.isJsonPrimitive()) {
                    String a = allowed.getAsJsonPrimitive().isString() ? allowed.getAsString() : allowed.toString();
                    String b = coercedEnumValue.getAsJsonPrimitive().isString() ? coercedEnumValue.getAsString() : coercedEnumValue.toString();
                    if (a.equals(b)) {
                        return coercedEnumValue;
                    }
                }
            }
            return coercedEnumValue;
        }

        return coerceByType(submitted, type, currentValue);
    }

    private static JsonElement coerceByType(JsonElement submitted, String type, JsonElement currentValue) {
        if (submitted == null || submitted.isJsonNull()) {
            return JsonNull.INSTANCE;
        }

        if (submitted.isJsonObject() || submitted.isJsonArray()) {
            return submitted.deepCopy();
        }

        String raw;
        if (submitted.isJsonPrimitive()) {
            JsonPrimitive p = submitted.getAsJsonPrimitive();
            if (p.isBoolean()) {
                raw = Boolean.toString(p.getAsBoolean());
            } else if (p.isNumber()) {
                raw = p.getAsNumber().toString();
            } else {
                raw = p.getAsString();
            }
        } else {
            raw = submitted.toString();
        }

        if (raw == null) {
            return JsonNull.INSTANCE;
        }

        raw = raw.trim();

        if (raw.isEmpty()) {
            if ("string".equals(type)) {
                return new JsonPrimitive("");
            }
            return JsonNull.INSTANCE;
        }

        switch (type != null ? type : "") {
            case "boolean":
                return new JsonPrimitive(parseBoolean(raw));
            case "integer":
                return new JsonPrimitive(parseInteger(raw));
            case "number":
                return new JsonPrimitive(parseNumber(raw));
            case "string", "object", "array":
                return new JsonPrimitive(raw);
            default:
                if (currentValue != null && !currentValue.isJsonNull()) {
                    String inferred = inferType(currentValue);
                    switch (inferred) {
                        case "boolean" -> {
                            return new JsonPrimitive(parseBoolean(raw));
                        }
                        case "integer" -> {
                            return new JsonPrimitive(parseInteger(raw));
                        }
                        case "number" -> {
                            return new JsonPrimitive(parseNumber(raw));
                        }
                        case "string" -> {
                            return new JsonPrimitive(raw);
                        }
                    }
                }

                if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
                    return new JsonPrimitive(Boolean.parseBoolean(raw));
                }

                // todo: remove this nonsense.
                try {
                    if (raw.matches("[-+]?\\d+")) {
                        return new JsonPrimitive(parseInteger(raw));
                    }
                    if (raw.matches("[-+]?((\\d*\\.\\d+)|(\\d+\\.\\d*))(?:[eE][-+]?\\d+)?") || raw.matches("[-+]?\\d+[eE][-+]?\\d+")) {
                        return new JsonPrimitive(parseNumber(raw));
                    }
                } catch (Exception ignored) {
                }

                return new JsonPrimitive(raw);
        }
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
        String v = raw.trim();
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException ex) {
            return new BigInteger(v);
        }
    }

    private static Number parseNumber(String raw) {
        String v = raw.trim();
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            return new BigDecimal(v);
        }
    }



    private static int parseSafeInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static JsonObject effectiveSchemaAtPath(JsonObject rootSchema, JsonElement documentJson, String path) {
        JsonObject root = rootSchema != null ? rootSchema : new JsonObject();
        if (path == null || path.isBlank()) {
            return SchemaUtil.normalizeSchema(root, rootSchema);
        }

        List<String> segments = JsonPathUtil.parsePath(path);
        JsonObject currentSchema = SchemaUtil.normalizeSchema(SchemaUtil.resolveRef(root, rootSchema), rootSchema);
        JsonElement currentValue = documentJson;

        for (String segment : segments) {
            currentSchema = SchemaUtil.normalizeSchema(currentSchema, rootSchema);
            //currentSchema = SchemaUtil.chooseMatchingBranch(currentSchema, currentValue, rootSchema);

            if (currentSchema.has("properties") && currentSchema.get("properties").isJsonObject()) {
                JsonObject props = currentSchema.getAsJsonObject("properties");
                if (props.has(segment) && props.get(segment).isJsonObject()) {
                    currentSchema = SchemaUtil.normalizeSchema(SchemaUtil.resolveRef(props.getAsJsonObject(segment), rootSchema), rootSchema);
                    currentValue = childValue(currentValue, segment);
                    continue;
                }
            }

            if (currentSchema.has("additionalProperties")) {
                JsonElement additional = currentSchema.get("additionalProperties");
                if (additional.isJsonObject()) {
                    currentSchema = SchemaUtil.normalizeSchema(SchemaUtil.resolveRef(additional.getAsJsonObject(), rootSchema), rootSchema);
                } else {
                    currentSchema = new JsonObject();
                }
                currentValue = childValue(currentValue, segment);
                continue;
            }

            if (currentSchema.has("items") && JsonPathUtil.isInteger(segment)) {
                JsonElement items = currentSchema.get("items");
                if (items != null && items.isJsonObject()) {
                    currentSchema = SchemaUtil.normalizeSchema(SchemaUtil.resolveRef(items.getAsJsonObject(), rootSchema), rootSchema);
                } else {
                    currentSchema = new JsonObject();
                }
                currentValue = childIndexValue(currentValue, parseSafeInt(segment, 0));
                continue;
            }

            currentValue = childValue(currentValue, segment);
        }

        currentSchema = SchemaUtil.normalizeSchema(currentSchema, rootSchema);
        // todo
        //currentSchema = SchemaUtil.chooseMatchingBranch(currentSchema, currentValue, rootSchema);
        return currentSchema == null ? new JsonObject() : currentSchema;
    }

    private static JsonElement childValue(JsonElement currentValue, String segment) {
        if (currentValue == null || currentValue.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (currentValue.isJsonObject()) {
            JsonObject obj = currentValue.getAsJsonObject();
            return obj.has(segment) ? obj.get(segment) : JsonNull.INSTANCE;
        }
        if (currentValue.isJsonArray() && JsonPathUtil.isInteger(segment)) {
            JsonArray arr = currentValue.getAsJsonArray();
            int idx = parseSafeInt(segment, -1);
            return idx >= 0 && idx < arr.size() ? arr.get(idx) : JsonNull.INSTANCE;
        }
        return JsonNull.INSTANCE;
    }

    private static JsonElement childIndexValue(JsonElement currentValue, int index) {
        if (currentValue == null || currentValue.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (currentValue.isJsonArray()) {
            JsonArray arr = currentValue.getAsJsonArray();
            return index >= 0 && index < arr.size() ? arr.get(index) : JsonNull.INSTANCE;
        }
        return JsonNull.INSTANCE;
    }
}