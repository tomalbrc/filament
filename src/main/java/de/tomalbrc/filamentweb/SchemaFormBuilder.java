package de.tomalbrc.filamentweb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import de.tomalbrc.filament.util.Json;
import de.tomalbrc.filamentweb.asset.Asset;
import de.tomalbrc.filamentweb.asset.AssetStore;
import de.tomalbrc.filamentweb.service.FragmentServlet;
import de.tomalbrc.filamentweb.util.SchemaUtil;
import de.tomalbrc.filamentweb.util.WebPaths;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static j2html.TagCreator.*;

public class SchemaFormBuilder {
    private static final String LABEL_STYLE = "flex: 0 0 25%; min-width: 0; max-width: 25%; text-overflow: ellipsis; white-space: nowrap;";

    private static String safeId(String prefix, String uuid, String path) {
        String raw = prefix + "-" + uuid + "-" + (path == null ? "" : path);
        return raw.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static String objectCollapseId(String uuid, String path) {
        return safeId("collapse-object", uuid, path);
    }

    private static String arrayCollapseId(String uuid, String path) {
        return safeId("collapse-array", uuid, path);
    }

    private static String fieldsContainerId(String uuid, String path) {
        return safeId("fields-container", uuid, path);
    }

    private static String arrayContainerId(String uuid, String path) {
        return safeId("array-container", uuid, path);
    }

    private static String composedContainerId(String uuid, String path) {
        return safeId("composed-container", uuid, path);
    }

    private static String composedChoiceId(String uuid, String path) {
        return safeId("composed-choice", uuid, path);
    }

    private static JsonElement copyElement(JsonElement element) {
        return JsonParser.parseString(element.toString());
    }

    private static JsonObject deepCopy(JsonObject obj) {
        return JsonParser.parseString(obj.toString()).getAsJsonObject();
    }

    private static boolean hasAllOf(JsonObject schema) {
        return schema != null && schema.has("allOf") && schema.get("allOf").isJsonArray();
    }

    private static boolean hasAnyOfOrOneOf(JsonObject schema) {
        return schema != null && (schema.has("anyOf") || schema.has("oneOf"));
    }

    private static void mergeRequired(JsonObject target, JsonObject source) {
        Set<String> required = new LinkedHashSet<>();

        if (target.has("required") && target.get("required").isJsonArray()) {
            for (JsonElement e : target.getAsJsonArray("required")) required.add(e.getAsString());
        }

        if (source.has("required") && source.get("required").isJsonArray()) {
            for (JsonElement e : source.getAsJsonArray("required")) required.add(e.getAsString());
        }

        if (!required.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (String r : required) arr.add(r);
            target.add("required", arr);
        }
    }

    private static JsonObject mergeSchemaObjects(JsonObject base, JsonObject overlay) {
        JsonObject result = deepCopy(base);

        for (Map.Entry<String, JsonElement> entry : overlay.entrySet()) {
            String key = entry.getKey();

            if ("properties".equals(key) && entry.getValue().isJsonObject()) {
                JsonObject mergedProps = result.has("properties") && result.get("properties").isJsonObject()
                        ? result.getAsJsonObject("properties")
                        : new JsonObject();

                for (Map.Entry<String, JsonElement> prop : entry.getValue().getAsJsonObject().entrySet()) {
                    mergedProps.add(prop.getKey(), copyElement(prop.getValue()));
                }
                result.add("properties", mergedProps);
                continue;
            }

            if ("required".equals(key) && entry.getValue().isJsonArray()) {
                mergeRequired(result, overlay);
                continue;
            }

            if ("anyOf".equals(key) || "oneOf".equals(key) || "allOf".equals(key)) {
                continue;
            }

            result.add(key, copyElement(entry.getValue()));
        }

        mergeRequired(result, overlay);
        return result;
    }

    private static JsonObject resolveRenderableObjectSchema(JsonObject node, JsonElement data, JsonObject rootSchema) {
        JsonObject normalized = SchemaUtil.normalizeSchema(node, rootSchema);
        JsonObject resolved = deepCopy(normalized);

        if (hasAllOf(normalized)) {
            for (JsonElement branchEl : normalized.getAsJsonArray("allOf")) {
                if (!branchEl.isJsonObject()) continue;
                JsonObject branch = SchemaUtil.normalizeSchema(branchEl.getAsJsonObject(), rootSchema);
                resolved = mergeSchemaObjects(resolved, branch);
            }
        }

        if (hasAnyOfOrOneOf(normalized)) {
            List<JsonObject> branches = SchemaUtil.extractBranches(normalized, rootSchema);
            if (!branches.isEmpty()) {
                int selected = SchemaUtil.pickBranchIndex(data, branches, rootSchema);
                if (selected < 0 || selected >= branches.size()) selected = 0;
                resolved = branches.get(selected);
            }
        }

        return resolved;
    }

    private static String description(JsonObject schema) {
        return (schema != null && schema.has("description")) ? schema.get("description").getAsString() : null;
    }

    private static Tag<?> renderRemoveButton(String uuid, String path, String key, String targetId) {
        return button(rawHtml("&times;"))
                .withType("button")
                .withClass("btn btn-sm btn-link text-danger text-decoration-none fw-bold ms-1")
                .withStyle("background: #149;")
                .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.REMOVE_OBJECT.toString(), uuid, path))
                .attr("hx-target", "#" + targetId)
                .attr("hx-swap", "outerHTML")
                .attr("hx-vals", "{\"key\": \"" + key + "\"}");
    }

    private static ContainerTag<?> renderCollapsibleSection(String title, String tooltip, String collapseId, Tag<?> content) {
        var btn = button(title)
                .withType("button")
                .withClass("btn btn-sm btn-link p-0 text-decoration-none fw-bold text-start hint--left hint--no-arrow")
                .attr("data-bs-toggle", "collapse")
                .attr("data-bs-target", "#" + collapseId)
                .attr("aria-expanded", "true");

        if (tooltip != null) {
            btn.withTitle(tooltip).attr("aria-label", tooltip);
        }

        var header = div().withClass("gap-2 mb-1").with(btn);
        var body = div().withId(collapseId).withClass("collapse hide").with(content);

        return div().withClass("border rounded p-1 bg-body-tertiary shadow-sm flex-grow-1").with(header, body);
    }

    private static ContainerTag<?> renderCustomKeyEditor(String uuid, String objectPath, String currentKey) {
        String safe = safeId("custom-key-editor", uuid, (objectPath == null ? "" : objectPath) + "-" + currentKey);
        return div().withClass("input-group input-group-sm mb-1").with(
                input().withType("text").withClass("form-control").withId(safe).withName("newKey").withValue(currentKey).withPlaceholder("Rename key..."),
                button("Rename").withType("button").withClass("btn btn-outline-secondary")
                        .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.RENAME_OBJECT.toString(), uuid, objectPath, currentKey))
                        .attr("hx-include", "#" + safe)
                        .attr("hx-target", "#" + fieldsContainerId(uuid, objectPath))
                        .attr("hx-swap", "outerHTML")
        );
    }

    private static Tag<?> renderPrimitiveInput(JsonObject schema, String name, JsonElement value, String id, String uuid) {
        if (schema == null) schema = new JsonObject();
        String updateUrl = uuid != null ? WebPaths.fragment(FragmentServlet.Operation.UPDATE_FIELD.toString(), uuid, name) : null;

        if (schema.has("const")) {
            String constValue = schema.get("const").isJsonNull() ? "" : schema.get("const").getAsString();
            return input().withType("text").withClass("form-control form-control-sm").withValue(constValue).attr("readonly", "readonly");
        }

        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            var select = select().withName(name).withClass("form-select form-select-sm");
            if (id != null) select.withId(id);
            String selected = value != null && value.isJsonPrimitive() ? value.getAsString() : null;
            select.with(option("").withValue(""));
            for (JsonElement ev : schema.getAsJsonArray("enum")) {
                String opt = ev.isJsonNull() ? "" : ev.getAsString();
                var optTag = option(opt).withValue(opt);
                if (Objects.equals(opt, selected)) optTag.attr("selected", "selected");
                select.with(optTag);
            }
            if (updateUrl != null) select
                    .attr("hx-post", updateUrl)
                    .attr("hx-swap", "none")
                    .attr("hx-trigger", "change");

            return select;
        }

        String type = SchemaUtil.schemaType(schema);
        String val = value != null && value.isJsonPrimitive() ? value.getAsString() : "";

        switch (type != null ? type : "string") {
            case "boolean": {
                boolean checked = value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean() && value.getAsBoolean();
                var hiddenFalse = input().withType("hidden").withName(name).withValue("false");
                var cb = input().withType("checkbox").withName(name).withClass("form-check-input").withValue("true");
                if (id != null) cb.withId(id);
                if (checked) cb.attr("checked", "checked");
                if (updateUrl != null) cb.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "change");
                return span().with(hiddenFalse, cb);
            }
            case "number":
            case "integer": {
                var num = input().withType("number").withName(name).withClass("form-control form-control-sm").withValue(val);
                if (id != null) num.withId(id);
                if (updateUrl != null) num.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "change delay:300ms");
                return num;
            }
            default: {
                var txt = input().withType("text").withName(name).withClass("form-control form-control-sm").withValue(val);
                if (id != null) txt.withId(id);
                if (updateUrl != null) txt.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "changed delay:300ms, blur");
                return txt;
            }
        }
    }

    private static Tag<?> renderSchemaValue(String uuid, String path, JsonObject schema, JsonElement value, JsonObject rootSchema, String inputId) {
        JsonObject normalized = SchemaUtil.normalizeSchema(schema, rootSchema);
        JsonObject resolved = resolveRenderableObjectSchema(normalized, value, rootSchema);

        if (SchemaUtil.hasComposableSchema(normalized)) return renderComposedFieldFragment(uuid, path, normalized, value, rootSchema);
        if (SchemaUtil.isObjectSchema(resolved)) return renderInlineObjectField(uuid, path, resolved, value, rootSchema);
        if (SchemaUtil.isArraySchema(resolved)) return renderArrayField(resolved, value, path, uuid, rootSchema);
        return renderPrimitiveInput(resolved, path, value, inputId, uuid);
    }

    // anyOf / oneOf
    public static ContainerTag<?> renderComposedFieldFragment(String uuid, String path, JsonObject schema, JsonElement value, JsonObject rootSchema) {
        JsonObject normalized = SchemaUtil.normalizeSchema(schema, rootSchema);
        List<JsonObject> branches = SchemaUtil.extractBranches(normalized, rootSchema);
        if (branches.isEmpty()) branches = List.of(normalized);

        int selected = SchemaUtil.pickBranchIndex(value, branches, rootSchema);
        if (selected < 0 || selected >= branches.size()) selected = 0;

        String containerId = composedContainerId(uuid, path);
        String choiceId = composedChoiceId(uuid, path);
        String title = SchemaUtil.schemaDisplayName(normalized, path, "Value");

        var choice = select().withId(choiceId).withClass("form-select form-select-sm").withName("choice");
        for (int i = 0; i < branches.size(); i++) {
            String label = SchemaUtil.schemaLabel(branches.get(i), i);
            var opt = option(label).withValue(String.valueOf(i));
            if (i == selected) opt.attr("selected", "selected");
            choice.with(opt);
        }

        choice
                .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.UPDATE_CHOICE.toString(), uuid, path))
                .attr("hx-target", "#" + containerId)
                .attr("hx-swap", "outerHTML")
                .attr("hx-trigger", "change")
                .attr("hx-include", "this")
                .attr("hx-vals", "js:{choice: event.target.value}");

        Tag<?> branchEditor = renderSchemaValue(uuid, path, branches.get(selected), value, rootSchema, safeId("field", uuid, path));

        var lbl = label(title).withClass("form-label mb-0 hint--left hint--no-arrow").withStyle(LABEL_STYLE);
        String desc = description(normalized);
        if (desc != null) lbl.withTitle(desc).attr("aria-label", desc);

        return div().withId(containerId).withClass("d-flex flex-column gap-2 border rounded p-2 mb-2 bg-body-secondary flex-grow-1").with(
                div().withClass("d-flex align-items-center gap-2").with(
                        lbl,
                        div().withClass("flex-grow-1").with(choice)
                ),
                branchEditor
        );
    }

    public static ContainerTag<?> renderPane(String uuid) {
        Asset asset = AssetStore.getAsset(UUID.fromString(uuid));
        if (asset == null) throw new IllegalArgumentException();

        String displayName = asset.data.id().toString();
        var regItem = BuiltInRegistries.ITEM.get(asset.data.id());
        if (regItem.isPresent()) {
            var holder = regItem.get();
            List<DomContent> elements = new ArrayList<>();

            Component s = holder.value().getDefaultInstance().getItemName();
            s.visit((style,string) -> {
                if (string.isEmpty()) {
                    return Optional.empty(); // Skip empty segments to keep the HTML clean
                }

                var span = span(string);
                StringBuilder css = new StringBuilder();

                TextColor color = style.getColor();
                if (color != null) {
                    css.append(String.format("color: #%06X;", color.getValue() & 0xFFFFFF));
                }

                Integer shadow = style.getShadowColor();
                if (shadow != null) {
                    css.append(String.format("text-shadow: 1px 1px 0 #%06X;", shadow & 0xFFFFFF));
                }

                if (style.isBold()) css.append("font-weight: bold;");
                if (style.isItalic()) css.append("font-style: italic;");

                boolean strike = style.isStrikethrough();
                boolean under = style.isUnderlined();
                if (strike && under) {
                    css.append("text-decoration: underline line-through;");
                } else if (strike) {
                    css.append("text-decoration: line-through;");
                } else if (under) {
                    css.append("text-decoration: underline;");
                }

                if (style.isObfuscated()) {
                    span.withClass("mc-obfuscated");
                }

                if (!css.isEmpty()) {
                    span.withStyle(css.toString().trim());
                }

                elements.add(span);

                return Optional.empty();
            }, Style.EMPTY);

            displayName = div().withClass("mc-component").with(elements).render();
        }

        JsonElement contentJson = asset.readJson();

        var paneFormContainer = div()
                .withClass("p-0 vh-100 d-flex flex-column overflow-hidden")
                .withId("pane-" + uuid)
                .with(
                        nav().withId("file-navbar").withClass("navbar bg-body-tertiary border-bottom").withStyle("height: 55px;").with(
                                div().withClass("container-fluid d-flex align-items-center").with(
                                        span(rawHtml(displayName)).withClass("navbar-brand mb-0 h1 me-auto"),
                                        div().withClass("d-flex flex-grow-1 justify-content-center").with(
                                                div().withClass("btn-group").attr("role", "group").with(
                                                        button("Write to file").withClass("btn btn-sm btn-outline-primary"),
                                                        button("Reload behaviours").withClass("btn btn-sm btn-outline-primary")
                                                )
                                        ),
                                        div().withClass("flex-grow-1")
                                )
                        )
                );

        if (asset.getSchema() != null && asset.getSchema().isJsonObject()) {
            JsonObject schemaObj = asset.getSchema().getAsJsonObject();
            var jsonPreviewSection = renderJsonPreviewFragment(uuid, contentJson, false);

            Tag<?> modelSection = renderModelViewerFragment(uuid, false);

            paneFormContainer.with(
                    div().withClass("flex-grow-1 overflow-auto p-2").with(
                            form().withClass("json-editor-form").with(
                                    input().withType("hidden").withName("name").withValue(uuid),
                                    div().withClass("row g-0").with(
                                            div().withClass("col-md-4 g-0").with(modelSection, jsonPreviewSection),
                                            div().withClass("col-md-8 g-0 px-1").with(
                                                    renderObjectFieldsContainer(uuid, "", schemaObj, contentJson, schemaObj)
                                            )
                                    ),
                                    div().withId("save-status-" + uuid)
                            )
                    )
            );
        }
        return paneFormContainer;
    }

    public static ContainerTag<?> renderObjectFieldsContainer(String uuid, String path, JsonObject schema, JsonElement data, JsonObject rootSchema) {
        return div().withId(fieldsContainerId(uuid, path == null ? "" : path)).withClass("object-fields").with(
                buildFieldsFromSchema(schema, data, path == null ? "" : path, uuid, rootSchema)
        );
    }

    private static ContainerTag<?> buildFieldsFromSchema(JsonObject schema, JsonElement data, String prefix, String uuid, JsonObject rootSchema) {
        var container = div();

        JsonObject node = schema;
        node = resolveRenderableObjectSchema(node, data, rootSchema);

        JsonObject props = node.has("properties") && node.get("properties").isJsonObject()
                ? node.getAsJsonObject("properties")
                : new JsonObject();

        JsonObject additionalPropsSchema = null;
        if (node.has("additionalProperties") && node.get("additionalProperties").isJsonObject()) {
            additionalPropsSchema = node.getAsJsonObject("additionalProperties");
        }

        Set<String> requiredFields = new HashSet<>();
        if (node.has("required") && node.get("required").isJsonArray()) {
            node.getAsJsonArray("required").forEach(e -> requiredFields.add(e.getAsString()));
        }

        JsonObject dataObj = (data != null && data.isJsonObject()) ? data.getAsJsonObject() : new JsonObject();
        Set<String> allKeys = new LinkedHashSet<>(props.keySet());
        allKeys.addAll(dataObj.keySet());

        for (String propName : allKeys) {
            boolean inSchema = props.has(propName);
            boolean isRequired = requiredFields.contains(propName);
            if (!dataObj.has(propName) && !isRequired) continue;

            JsonObject propSchema;
            if (inSchema) {
                propSchema = SchemaUtil.resolveSchema(props.getAsJsonObject(propName), rootSchema);
            } else if (additionalPropsSchema != null) {
                propSchema = SchemaUtil.resolveSchema(additionalPropsSchema, rootSchema);
            } else {
                propSchema = new JsonObject();
            }

            String path = SchemaUtil.normalizedPath(prefix, propName);
            String idSafe = safeId("field", uuid, path);

            boolean nestedBlock = SchemaUtil.isNestedSchemaBlock(propSchema) || hasAnyOfOrOneOf(propSchema) || hasAllOf(propSchema);

            var fieldWrapper = div().withClass("border-start ps-2 mb-1 d-flex flex-grow-1").withId("wrap-" + idSafe);

            if (nestedBlock) {
                if (!inSchema) fieldWrapper.with(renderCustomKeyEditor(uuid, prefix, propName));
                fieldWrapper.with(renderSchemaValue(uuid, path, propSchema, dataObj.get(propName), rootSchema, idSafe));
                if (!isRequired) fieldWrapper.with(renderRemoveButton(uuid, prefix, propName, fieldsContainerId(uuid, prefix)));
            } else {
                var row = div().withClass("d-flex flex-grow-1 align-items-center").with(
                        renderFieldHeader(uuid, prefix, propName, propSchema, inSchema, idSafe),
                        div().withClass("flex-grow-1").with(renderSchemaValue(uuid, path, propSchema, dataObj.get(propName), rootSchema, idSafe))
                );

                if (!isRequired) row.with(renderRemoveButton(uuid, prefix, propName, fieldsContainerId(uuid, prefix)));
                //if (!inSchema) fieldWrapper.with(renderCustomKeyEditor(uuid, prefix, propName));
                fieldWrapper.with(row);
            }
            container.with(fieldWrapper);
        }

        String s = (prefix == null || prefix.isEmpty() ? "root" : prefix).replaceAll("[^a-zA-Z0-9_-]", "_");
        List<String> missingOptional = props.keySet().stream().filter(p -> !dataObj.has(p) && !requiredFields.contains(p)).sorted().toList(); // todo: sort by type, primitives first

        if (!missingOptional.isEmpty()) {
            String pickerId = "picker-" + s;
            container.with(div().withClass("input-group input-group-sm mt-3").with(
                    select().withClass("form-select").withId(pickerId).with(option("Add property...").withValue(""), each(missingOptional, p -> option(p).withValue(p))),
                    button("Add").withType("button").withClass("btn btn-primary")
                            .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_OBJECT.toString(), uuid, prefix))
                            .attr("hx-vals", "js:{key: document.getElementById('" + pickerId + "').value}")
                            .attr("hx-target", "#" + fieldsContainerId(uuid, prefix)).attr("hx-swap", "outerHTML")
            ));
        }

        boolean allowAdditional = !node.has("additionalProperties") ||
                !node.get("additionalProperties").isJsonPrimitive() ||
                node.get("additionalProperties").getAsBoolean();

        if (allowAdditional) {
            String customKeyId = "custom-key-" + s;
            String typeSelectId = "custom-type-" + s;

            var typeSelect = select().withClass("form-select").withId(typeSelectId);

            if (additionalPropsSchema != null && hasAnyOfOrOneOf(additionalPropsSchema)) {
                List<JsonObject> branches = SchemaUtil.extractBranches(additionalPropsSchema, rootSchema);
                for (int i = 0; i < branches.size(); i++) {
                    typeSelect.with(option(SchemaUtil.schemaLabel(branches.get(i), i)).withValue("index:" + i));
                }
            } else if (additionalPropsSchema != null && additionalPropsSchema.has("type")) {
                String typeName = additionalPropsSchema.get("type").getAsString();
                typeSelect.with(option(typeName.substring(0, 1).toUpperCase() + typeName.substring(1)).withValue(typeName));
            } else {
                typeSelect.with(
                        option("String").withValue("string"),
                        option("Number").withValue("number"),
                        option("Boolean").withValue("boolean"),
                        option("Object").withValue("object"),
                        option("Array").withValue("array")
                );
            }

            container.with(div().withClass("input-group input-group-sm mt-2 shadow-sm").with(
                    input().withType("text").withClass("form-control").withId(customKeyId).withPlaceholder("New key name..."),
                    typeSelect,
                    button("Add Entry").withType("button").withClass("btn btn-primary")
                            .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_OBJECT.toString(), uuid, prefix))
                            .attr("hx-vals", "js:{key: document.getElementById('" + customKeyId + "').value, type: document.getElementById('" + typeSelectId + "').value}")
                            .attr("hx-target", "#" + fieldsContainerId(uuid, prefix))
                            .attr("hx-swap", "outerHTML")
            ));
        }

        return container;
    }

    private static Tag<?> renderFieldHeader(
            String uuid,
            String objectPath,
            String currentKey,
            JsonObject propSchema,
            boolean inSchema,
            String idSafe
    ) {
        if (inSchema) {
            var lbl = label(small(SchemaUtil.schemaDisplayName(propSchema, currentKey, currentKey)))
                    .attr("for", idSafe)
                    .withClass("form-label mb-0 hint--left hint--no-arrow")
                    .withStyle(LABEL_STYLE);

            String desc = description(propSchema);
            if (desc != null) lbl.withTitle(desc).attr("aria-label", desc);

            return lbl;
        }

        String safe = safeId("custom-key-editor", uuid, (objectPath == null ? "" : objectPath) + "-" + currentKey);

        return div().withClass("input-group input-group-sm").withStyle(LABEL_STYLE).with(
                input().withType("text")
                        .withClass("form-control")
                        .withId(safe)
                        .withName("newKey")
                        .withValue(currentKey)
                        .withPlaceholder("Rename key..."),
                button("Rename").withType("button").withClass("btn btn-outline-secondary")
                        .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.RENAME_OBJECT.toString(), uuid, objectPath, currentKey))
                        .attr("hx-include", "#" + safe)
                        .attr("hx-target", "#" + fieldsContainerId(uuid, objectPath))
                        .attr("hx-swap", "outerHTML")
        );
    }

    private static Tag<?> renderInlineObjectField(String uuid, String path, JsonObject schema, JsonElement value, JsonObject rootSchema) {
        String title = SchemaUtil.schemaDisplayName(schema, path, "Object");
        String desc = description(schema);
        return renderCollapsibleSection(title, desc, objectCollapseId(uuid, path), renderObjectFieldsContainer(uuid, path, schema, value, rootSchema));
    }

    private static Tag<?> renderArrayField(JsonObject node, JsonElement data, String path, String uuid, JsonObject rootSchema) {
        String title = SchemaUtil.schemaDisplayName(node, path, "Array");
        String desc = description(node);
        JsonObject itemsSchema = node.has("items") && node.get("items").isJsonObject() ? SchemaUtil.resolveSchema(node.getAsJsonObject("items"), rootSchema) : new JsonObject();
        JsonArray arr = data != null && data.isJsonArray() ? data.getAsJsonArray() : new JsonArray();
        return renderCollapsibleSection(title, desc, arrayCollapseId(uuid, path), renderArrayContainer(uuid, path, itemsSchema, arr, rootSchema));
    }

    public static ContainerTag<?> renderArrayContainer(String uuid, String path, JsonObject itemsSchema, JsonArray arr, JsonObject root) {
        String safe = arrayContainerId(uuid, path);
        var container = div().withId(safe).withClass("array-container border rounded p-2 mb-1 bg-body-tertiary shadow-sm");
        for (int i = 0; i < arr.size(); i++) container.with(renderArrayItemElement(uuid, path, itemsSchema, arr.get(i), i, root));
        return container.with(
                button("+ Add Item").withType("button").withClass("btn btn-sm btn-outline-primary w-100 mt-1")
                        .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_ARRAY.toString(), uuid, path))
                        .attr("hx-target", "#" + safe).attr("hx-swap", "outerHTML")
        );
    }

    public static Tag<?> renderArrayItemElement(String uuid, String path, JsonObject itemsSchema, JsonElement itemValue, int index, JsonObject root) {
        String itemPrefix = path + "[" + index + "]";
        Tag<?> editor = renderSchemaValue(uuid, itemPrefix, SchemaUtil.normalizeSchema(itemsSchema, root), itemValue, root, safeId("field", uuid, itemPrefix));
        return div().withClass("array-item d-flex align-items-start gap-2 mb-1 pb-1 border-bottom").with(
                div().withClass("flex-grow-1").with(editor),
                renderRemoveButton(uuid, path, String.valueOf(index), arrayContainerId(uuid, path))
        );
    }

    public static ContainerTag<?> renderJsonPreviewFragment(String uuid, JsonElement documentJson, boolean oob) {
        var t = pre().withId("json-preview-" + uuid).withClass("theme-atom-one-dark").with(
                code(documentJson != null ? SchemaUtil.gson.toJson(documentJson) : "Error! No Document!").withClass("language-json text-muted").attr("style", "font-size: 0.7rem;")
        );

        if (oob) return t.attr("hx-swap-oob", true);
        return t;
    }

    public static ContainerTag<?> renderModelViewerFragment(String uuid, boolean oob) {
        Asset asset = AssetStore.getAsset(UUID.fromString(uuid));

        var models = resolveModel(asset);
        String previewId = "model-viewer-" + uuid;

        if (models.isEmpty()) {
            var t = div()
                    .withId(previewId)
                    .withClass("border rounded bg-black d-flex align-items-center justify-content-center text-muted")
                    .withText("No Preview Available");
            if (oob) return t.attr("hx-swap-oob", true);
            return t;
        }

        var wrapper = div()
                .withId(previewId)
                .withClass("d-flex flex-wrap gap-1");

        int i = 0;
        for (var entry : models) {
            byte[] modelBytes = entry.data();
            if (modelBytes == null || modelBytes.length == 0) {
                continue;
            }

            String modelId = previewId + "-" + i++;
            String label = entry.path() == null ? "" : entry.path();
            String label1 = entry.containerPath() == null ? "" : entry.containerPath();
            String label2 = entry.id() == null ? "" : entry.id();
            String src = "data:model/gltf-binary;base64," + Base64.getEncoder().encodeToString(modelBytes);

            var card = div()
                    .withClass("border bg-black flex-column p-1")
                    .withStyle("min-width: 150px; flex: 1 1 100px;")
                    .with(
                            div()
                                    .withClass("text-muted small mb-2 text-wrap")
                                    .withText(label),
                            div()
                                    .withClass("text-muted small mb-2 text-wrap")
                                    .withText(label1),
                            div()
                                    .withClass("text-muted small mb-2 text-wrap")
                                    .withText(label2)
                    )
                    .with(
                            rawHtml(String.format(
                                    "<model-viewer id=\"%s\" src=\"%s\" interaction-prompt=\"none\" camera-controls camera-orbit=\"45deg 55deg 4m\" auto-rotate style=\"width:100%%;height:400px;\"></model-viewer>",
                                    modelId,
                                    src
                            ))
                    );

            wrapper.with(card);
        }

        if (i == 0) {
            var t = div()
                    .withId(previewId)
                    .withClass("border rounded bg-black d-flex align-items-center justify-content-center text-muted")
                    .withText("No Preview Available");
            if (oob) return t.attr("hx-swap-oob", true);
            return t;
        }

        if (oob) return wrapper.attr("hx-swap-oob", true);
        return wrapper;
    }

    record Model(String path, String containerPath, String id, String assetPath, byte[] data) {

    }

    private static List<Model> resolveModel(Asset asset) {
        List<Model> models = new ArrayList<>();

        try {
            ResourceProvider res = null;
            Identifier itemModel = asset.data.id();

            var item = BuiltInRegistries.ITEM.get(asset.data.id());
            if (item.isPresent()) {
                itemModel = asset.data.components().get(DataComponents.ITEM_MODEL);
                if (itemModel == null) {
                    itemModel = asset.data.itemModel();
                    if (itemModel == null) {
                        itemModel = item.get().value().getDefaultInstance().get(DataComponents.ITEM_MODEL);
                    }

                    var source = itemModel == null ? null : EditorServer.resourcePackBuilder().getDataOrSource(AssetPaths.itemAsset(itemModel));
                    if (source == null) itemModel = asset.data.vanillaItem().getDefaultInstance().get(DataComponents.ITEM_MODEL);
                }

            }

            String type = "item_resource";
            if (asset.data instanceof ItemData id) {
                res = id.itemResource();
            }
            else if (asset.data instanceof BlockData bd) {
                res = bd.blockResource() != null ? bd.blockResource() : bd.itemResource();
                type = "item_resource/block_resource";
            }
            else if (asset.data instanceof DecorationData dd) {
                res = dd.itemResource();
            }

            if (res != null && !res.getModels().isEmpty()) {
                for (Map.Entry<String, Identifier> entry : res.getModels().entrySet()) {
                    models.add(new Model(type, null, entry.getKey(), AssetPaths.model(entry.getValue()), EditorServer.CONVERTER.toGlb(entry.getValue())));
                }
            }

            if (itemModel != null) {
                var source = EditorServer.resourcePackBuilder().getDataOrSource(AssetPaths.itemAsset(itemModel));
                if (source != null) {
                    var el = JsonParser.parseReader(
                            new InputStreamReader(new ByteArrayInputStream(source), StandardCharsets.UTF_8)
                    );

                    if (el.isJsonObject()) {
                        collectModels(itemModel, el, "", models);
                    }
                }
            }

        } catch (Exception ignored) {}

        return models;
    }

    private static void collectModels(Identifier itemModel, JsonElement el, String path, List<Model> models) {
        if (el == null || el.isJsonNull()) return;

        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                String newPath = path.isEmpty() ? key : path + "." + key;

                if ("model".equals(key) && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    try {
                        Identifier id = Identifier.tryParse(value.getAsString());
                        if (id != null && !id.getPath().isBlank())
                            models.add(new Model(id.toString(), AssetPaths.itemAsset(itemModel), newPath, AssetPaths.model(id) + ".json", EditorServer.CONVERTER.toGlb(id)));
                    } catch (Exception ignored) {}
                }

                collectModels(itemModel, value, newPath, models);
            }
        }
        else if (el.isJsonArray()) {
            int i = 0;
            for (JsonElement child : el.getAsJsonArray()) {
                collectModels(itemModel, child, path + "[" + i++ + "]", models);
            }
        }
    }
}