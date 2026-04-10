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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static j2html.TagCreator.span;

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

        JsonObject schemaRoot = asset.getSchema() != null && asset.getSchema().isJsonObject()
                ? asset.getSchema().getAsJsonObject()
                : new JsonObject();

        JsonElement documentJson = asset.getJson();

        switch (op) {
            case UPDATE_FIELD: {
                JsonObject fieldSchema = SchemaUtil.effectiveSchemaAtPath(schemaRoot, documentJson, path);
                JsonElement currentValue = JsonPathUtil.getElementAtPath(documentJson, path);
                String submittedRaw = extractSubmittedRawValue(req, path, value);
                JsonElement submitted = submittedRaw == null ? JsonNull.INSTANCE : new JsonPrimitive(submittedRaw);
                JsonElement coerced = SchemaUtil.coerceSubmittedValue(submitted, fieldSchema, currentValue);

                JsonPathUtil.setValueAtPath(documentJson, path, coerced);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                resp.getWriter().write(span(asset.isDirty() ? "Unsaved Changes!":"").withId("unsaved-changes").attr("hx-swap-oob", "true").attr("hx-swap-oob", "innerHTML").render());
                return;
            }

            case UPDATE_CHOICE: {
                JsonObject nodeSchema = SchemaUtil.effectiveSchemaAtPath(schemaRoot, documentJson, path);
                List<JsonObject> branches = SchemaUtil.extractBranches(nodeSchema, schemaRoot);

                if (branches.isEmpty()) {
                    resp.setStatus(400);
                    resp.getWriter().write("No schema branches available");
                    return;
                }

                int selectedIndex = SchemaUtil.parseSafeInt(req.getParameter("choice"), 0);
                if (selectedIndex < 0 || selectedIndex >= branches.size()) {
                    selectedIndex = 0;
                }

                JsonObject selectedBranch = branches.get(selectedIndex);
                JsonElement replacement = SchemaUtil.createDefaultForSchema(selectedBranch, schemaRoot);
                JsonPathUtil.setValueAtPath(documentJson, path, replacement);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderComposedFieldFragment(uuid, path, nodeSchema, replacement, schemaRoot).render());
                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                resp.getWriter().write(span(asset.isDirty() ? "Unsaved Changes!":"").withId("unsaved-changes").attr("hx-swap-oob", "true").attr("hx-swap-oob", "innerHTML").render());

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

                JsonObject arrSchema = SchemaUtil.effectiveSchemaAtPath(schemaRoot, documentJson, path);
                JsonObject itemsSchema = arrSchema.has("items") && arrSchema.get("items").isJsonObject()
                        ? SchemaUtil.resolveRef(arrSchema.getAsJsonObject("items"), schemaRoot)
                        : new JsonObject();

                JsonElement newItem = SchemaUtil.createDefaultForSchema(itemsSchema, schemaRoot);
                arr.add(newItem);
                asset.apply(documentJson);

                resp.getWriter().write(SchemaFormBuilder.renderArrayContainer(uuid, path, arrSchema, itemsSchema, arr, schemaRoot).render());
                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                resp.getWriter().write(span(asset.isDirty() ? "Unsaved Changes!":"").withId("unsaved-changes").attr("hx-swap-oob", "true").attr("hx-swap-oob", "innerHTML").render());

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
                    JsonObject objectSchema = SchemaUtil.effectiveSchemaAtPath(schemaRoot, documentJson, path);

                    if (op == Operation.ADD_OBJECT) {
                        String newKey = req.getParameter("key");
                        if (newKey == null || newKey.isBlank()) {
                            newKey = "new_key";
                        }
                        newKey = newKey.trim();

                        if (!obj.has(newKey)) {
                            JsonObject propertySchema = schemaForObjectKey(objectSchema, schemaRoot, newKey);
                            JsonElement newVal;

                            // TODO: only create required fields for the "newVal" default value!
                            if ("components".equals(newKey) || "behaviour".equals(newKey)) newVal = new JsonObject();
                            else newVal = SchemaUtil.createDefaultForSchema(propertySchema, schemaRoot);

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

                    JsonObject arrSchema = SchemaUtil.effectiveSchemaAtPath(schemaRoot, documentJson, path);
                    JsonObject itemsSchema = arrSchema.has("items") && arrSchema.get("items").isJsonObject()
                            ? SchemaUtil.resolveRef(arrSchema.getAsJsonObject("items"), schemaRoot)
                            : new JsonObject();

                    resp.getWriter().write(SchemaFormBuilder.renderArrayContainer(uuid, path, arrSchema, itemsSchema, arr, schemaRoot).render());
                } else {
                    resp.setStatus(400);
                    resp.getWriter().write("Unsupported target type");
                    return;
                }

                resp.getWriter().write(SchemaFormBuilder.renderJsonPreviewFragment(uuid, documentJson, true).render());
                resp.getWriter().write(span(asset.isDirty() ? "Unsaved Changes!":"").withId("unsaved-changes").attr("hx-swap-oob", "true").attr("hx-swap-oob", "innerHTML").render());
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
        JsonObject resolved = SchemaUtil.resolveObjectMemberSchema(objectSchema, key, rootSchema);
        return resolved != null ? resolved : new JsonObject();
    }
}