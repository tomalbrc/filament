package de.tomalbrc.filamentweb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.resource.ResourceProvider;
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
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static j2html.TagCreator.*;

public class SchemaFormBuilder {
    private static final String LABEL_STYLE = "flex: 0 0 33%; min-width: 25%; max-width: 50%; text-overflow: ellipsis;";

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

    private static JsonObject resolveRenderableObjectSchema(JsonObject node, JsonElement data, JsonObject rootSchema) {
        JsonObject normalized = SchemaUtil.normalizeSchema(node, rootSchema);
        JsonObject resolved = SchemaUtil.deepCopy(normalized);

        if (SchemaUtil.hasAllOf(normalized)) {
            for (JsonElement branchEl : normalized.getAsJsonArray("allOf")) {
                if (!branchEl.isJsonObject()) continue;
                JsonObject branch = SchemaUtil.normalizeSchema(branchEl.getAsJsonObject(), rootSchema);
                resolved = SchemaUtil.mergeSchemaObjects(resolved, branch);
            }
        }

        if (SchemaUtil.hasAnyOfOrOneOf(normalized)) {
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
                .withClass("btn btn-sm btn-link btn-remove text-decoration-none fw-bold mr-1")
                .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.REMOVE_OBJECT.toString(), uuid, path))
                .attr("hx-target", "#" + targetId)
                .attr("hx-swap", "outerHTML")
                .attr("hx-vals", "{\"key\": \"" + key + "\"}");
    }

    private static ContainerTag<?> renderCollapsibleSection(String title, String tooltip, String collapseId, String extraClass, Tag<?> content) {
        var btn = button(title)
                .withType("button")
                .withClass("btn btn-sm btn-link p-0 text-decoration-none fw-bold text-start hint--left hint--no-arrow")
                .attr("data-bs-toggle", "collapse")
                .attr("data-bs-target", "#" + collapseId)
                .attr("aria-expanded", "false")
                .attr("aria-controls", collapseId);

        if (tooltip != null) {
            btn.withTitle(tooltip).attr("aria-label", tooltip);
        }

        var header = div().withClass("d-flex align-items-center justify-content-between gap-2 mb-1 collapse").with(btn);
        var body = div()
                .withId(collapseId)
                .withClass("collapse show")
                .attr("data-schema-collapse-key", collapseId)
                .with(content);

        return div().withClass("border rounded p-1 bg-body-tertiary " + extraClass).with(header, body);
    }

    private static ContainerTag<?> renderCollapsePersistenceScript() {
        // todo: sep script
        return script(rawHtml("""
                (function () {
                  const STORAGE_PREFIX = 'schema-form-collapse:';
                
                  function collapseKey(el) {
                    return el.getAttribute('data-schema-collapse-key');
                  }
                
                  function findToggle(el) {
                    const target = '#' + el.id;
                    return Array.from(document.querySelectorAll('[data-bs-target]')).find((btn) => btn.getAttribute('data-bs-target') === target);
                  }
                
                  function bind(root) {
                    const scope = root || document;
                    scope.querySelectorAll('[data-schema-collapse-key]').forEach((el) => {
                      if (el.dataset.schemaCollapseBound === '1') {
                        return;
                      }
                      el.dataset.schemaCollapseBound = '1';
                      el.addEventListener('shown.bs.collapse', () => {
                        sessionStorage.setItem(STORAGE_PREFIX + collapseKey(el), '1');
                      });
                      el.addEventListener('hidden.bs.collapse', () => {
                        sessionStorage.setItem(STORAGE_PREFIX + collapseKey(el), '0');
                      });
                    });
                  }
                
                  function apply(root) {
                    const scope = root || document;
                    scope.querySelectorAll('[data-schema-collapse-key]').forEach((el) => {
                      const stored = sessionStorage.getItem(STORAGE_PREFIX + collapseKey(el));
                      const expanded = stored === null ? true : stored === '1';
                      el.classList.add('collapse');
                      el.classList.toggle('show', expanded);
                      const toggle = findToggle(el);
                      if (toggle) {
                        toggle.classList.toggle('collapsed', !expanded);
                        toggle.setAttribute('aria-expanded', expanded ? 'true' : 'false');
                      }
                    });
                  }
                
                  document.addEventListener('DOMContentLoaded', () => {
                    bind(document);
                    apply(document);
                  });
                
                  document.addEventListener('htmx:afterSwap', () => {
                    bind(document);
                    apply(document);
                  });
                })();
                """));
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

    private static ContainerTag<?> buildFieldsFromSchema(JsonObject schema, JsonElement data, String prefix, String uuid, JsonObject rootSchema) {
        var container = div().withClass("schema-fields d-flex flex-column gap-1");

        JsonObject node = resolveRenderableObjectSchema(schema, data, rootSchema);

        JsonObject props = node.has("properties") && node.get("properties").isJsonObject()
                ? node.getAsJsonObject("properties")
                : new JsonObject();

        JsonObject dynamicEntrySchema = SchemaUtil.defaultEntrySchema(node, rootSchema);

        Set<String> requiredFields = new HashSet<>();
        if (node.has("required") && node.get("required").isJsonArray()) {
            node.getAsJsonArray("required").forEach(e -> requiredFields.add(e.getAsString()));
        }

        JsonObject dataObj = (data != null && data.isJsonObject()) ? data.getAsJsonObject() : new JsonObject();
        Set<String> allKeys = new LinkedHashSet<>(props.keySet());
        allKeys.addAll(dataObj.keySet());

        for (String propName : allKeys) {
            boolean explicitKey = props.has(propName);
            boolean isRequired = requiredFields.contains(propName);

            if (!dataObj.has(propName) && !isRequired) continue;

            JsonObject propSchema = SchemaUtil.resolveObjectMemberSchema(node, propName, rootSchema);
            String path = SchemaUtil.normalizedPath(prefix, propName);
            String idSafe = safeId("field", uuid, path);
            boolean nestedBlock = SchemaUtil.isNestedSchemaBlock(propSchema) || SchemaUtil.hasAnyOfOrOneOf(propSchema) || SchemaUtil.hasAllOf(propSchema);
            boolean customKey = !explicitKey && !matchesPatternProperties(node, propName);

            if (nestedBlock) {
                var block = div().withClass("schema-field-block d-flex flex-column gap-1").withId("wrap-" + idSafe);

                if (customKey) {
                    block.with(renderCustomKeyEditor(uuid, prefix, propName));
                }

                var valueAndRemove = div().withClass("d-flex align-items-start gap-2").with(
                        div().withClass("flex-grow-1").with(renderSchemaValue(uuid, path, propSchema, dataObj.get(propName), rootSchema, idSafe))
                );

                if (!isRequired) {
                    valueAndRemove.with(renderRemoveButton(uuid, prefix, propName, fieldsContainerId(uuid, prefix)));
                }

                block.with(valueAndRemove);
                container.with(block);
                continue;
            }

            Tag<?> header = renderFieldHeader(uuid, prefix, propName, propSchema, explicitKey || matchesPatternProperties(node, propName), idSafe);
            Tag<?> value = div().withClass("flex-grow-1 min-w-0").with(renderSchemaValue(uuid, path, propSchema, dataObj.get(propName), rootSchema, idSafe));

            var row = div().withClass("schema-field-row d-flex align-items-center gap-1").with(
                    header,
                    value
            );

            if (!isRequired) {
                row.with(renderRemoveButton(uuid, prefix, propName, fieldsContainerId(uuid, prefix)));
            }

            container.with(row);
        }

        String s = (prefix == null || prefix.isEmpty() ? "root" : prefix).replaceAll("[^a-zA-Z0-9_-]", "_");
        List<String> missingOptional = props.keySet().stream()
                .filter(p -> !dataObj.has(p) && !requiredFields.contains(p))
                .sorted().toList();

        String missingListId = "list-suggestions-" + s;
        Tag<?> missingDataList = datalist().withId(missingListId).with(each(missingOptional, p -> option().withValue(p)));

        if (!missingOptional.isEmpty()) {
            String pickerId = "picker-" + s;
            container.with(div().withClass("input-group input-group-sm mt-1").with(
                    input().withType("text").withClass("form-control").withId(pickerId).withPlaceholder("Add property...").attr("list", missingListId),
                    missingDataList,
                    button("Add").withType("button").withClass("btn btn-primary")
                            .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_OBJECT.toString(), uuid, prefix))
                            .attr("hx-vals", "js:{key: document.getElementById('" + pickerId + "').value}")
                            .attr("hx-target", "#" + fieldsContainerId(uuid, prefix))
                            .attr("hx-swap", "outerHTML")
            ));
        }

        boolean allowAdditional = !node.has("additionalProperties") || !node.get("additionalProperties").isJsonPrimitive() || node.get("additionalProperties").getAsBoolean();
        boolean hasPatternProperties = node.has("patternProperties") && node.get("patternProperties").isJsonObject() && !node.getAsJsonObject("patternProperties").entrySet().isEmpty();

        if (allowAdditional || hasPatternProperties) {
            String customKeyId = "custom-key-" + s;
            String typeSelectId = "custom-type-" + s;

            var typeSel = select().withId(typeSelectId).withClass("form-select").with(
                    option().withValue("string").withText("String"),
                    option().withValue("number").withText("Number"),
                    option().withValue("boolean").withText("Boolean"),
                    option().withValue("object").withText("Object"),
                    option().withValue("array").withText("Array")
            );

            if (SchemaUtil.hasAnyOfOrOneOf(dynamicEntrySchema)) {
                typeSel = select().withId(typeSelectId).withClass("form-select");
                List<JsonObject> branches = SchemaUtil.extractBranches(dynamicEntrySchema, rootSchema);
                for (int i = 0; i < branches.size(); i++) {
                    typeSel.with(option().withValue("index:" + i).withText(SchemaUtil.schemaLabel(branches.get(i), i)));
                }
            }

            if (allowAdditional) container.with(div().withClass("input-group input-group-sm mt-2 shadow-sm").with(
                    input().withType("text").withClass("form-control").withId(customKeyId).withPlaceholder("New key name...").attr("list", missingListId),
                    missingDataList,
                    typeSel,
                    button("Add Entry").withType("button").withClass("btn btn-primary")
                            .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_OBJECT.toString(), uuid, prefix))
                            .attr("hx-vals", "js:{key: document.getElementById('" + customKeyId + "').value, type: document.getElementById('" + typeSelectId + "').value}")
                            .attr("hx-target", "#" + fieldsContainerId(uuid, prefix))
                            .attr("hx-swap", "outerHTML")
            ));
        }

        return container;
    }

    private static Tag<?> renderPrimitiveInput(JsonObject schema, String name, JsonElement value, String id, String uuid) {
        if (schema == null) schema = new JsonObject();
        String updateUrl = uuid != null ? WebPaths.fragment(FragmentServlet.Operation.UPDATE_FIELD.toString(), uuid, name) : null;

        if (schema.has("const")) {
            String constValue = schema.get("const").isJsonNull() ? "" : schema.get("const").getAsString();
            return input().withType("text").withClass("form-control form-control-sm").withValue(constValue).attr("readonly", "readonly");
        }

        if (schema.has("enum") && schema.get("enum").isJsonArray()) {
            String listId = (id != null ? id : safeId("list", uuid, name)) + "-datalist";
            String selected = value != null && value.isJsonPrimitive() ? value.getAsString() : "";

            var dl = datalist().withId(listId).with(
                    each(schema.getAsJsonArray("enum").asList(), ev -> option().withValue(ev.isJsonNull() ? "" : ev.getAsString()))
            );

            var inp = input()
                    .withType("text")
                    .withName(name)
                    .withClass("form-control form-control-sm flex-grow-1")
                    .withValue(selected)
                    .attr("list", listId);

            if (id != null) inp.withId(id);
            if (updateUrl != null) inp
                    .attr("hx-post", updateUrl)
                    .attr("hx-swap", "none")
                    .attr("hx-trigger", "change, blur");

            return div().withClass("d-flex align-items-center gap-1 flex-grow-1").with(inp, dl);
        }

        String type = SchemaUtil.schemaType(schema);
        String val = value != null && value.isJsonPrimitive() ? value.getAsString() : "";

        switch (type != null ? type : "string") {
            case "boolean": {
                boolean checked = value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean() && value.getAsBoolean();
                var hiddenFalse = input().withType("hidden").withName(name).withValue("false");
                var cb = input().withType("checkbox").withName(name).withClass("form-check-input ms-1").withValue("true");
                if (id != null) cb.withId(id);
                if (checked) cb.attr("checked", "checked");
                if (updateUrl != null)
                    cb.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "change");
                return div().withClass("d-inline-flex align-items-center gap-1").with(hiddenFalse, cb);
            }
            case "number":
            case "integer": {
                var num = input().withType("number").withName(name).withClass("form-control form-control-sm flex-grow-1").withStyle("min-width: 0;").withValue(val);
                if (id != null) num.withId(id);
                if (updateUrl != null)
                    num.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "change delay:300ms");
                return num;
            }
            default: {
                var txt = input().withType("text").withName(name).withClass("form-control form-control-sm flex-grow-1").withStyle("min-width: 0;").withValue(val);
                if (id != null) txt.withId(id);
                if (updateUrl != null)
                    txt.attr("hx-post", updateUrl).attr("hx-swap", "none").attr("hx-trigger", "change delay:300ms, blur");
                return txt;
            }
        }
    }

    private static Tag<?> renderSchemaValue(String uuid, String path, JsonObject schema, JsonElement value, JsonObject rootSchema, String inputId) {
        JsonObject normalized = SchemaUtil.normalizeSchema(schema, rootSchema);
        JsonObject resolved = resolveRenderableObjectSchema(normalized, value, rootSchema);

        if (SchemaUtil.hasComposableSchema(normalized))
            return renderComposedFieldFragment(uuid, path, normalized, value, rootSchema);
        if (SchemaUtil.isObjectSchema(resolved))
            return renderInlineObjectField(uuid, path, resolved, value, rootSchema);
        if (SchemaUtil.isArraySchema(resolved)) return renderArrayField(resolved, value, path, uuid, rootSchema);
        return renderPrimitiveInput(resolved, path, value, inputId, uuid);
    }

    // anyOf / oneOf
    private static String composedCollapseId(String uuid, String path) {
        return safeId("collapse-composed", uuid, path);
    }

    public static ContainerTag<?> renderComposedFieldFragment(String uuid, String path, JsonObject schema, JsonElement value, JsonObject rootSchema) {
        JsonObject normalized = SchemaUtil.normalizeSchema(schema, rootSchema);
        List<JsonObject> branches = SchemaUtil.extractBranches(normalized, rootSchema);
        if (branches.isEmpty()) branches = List.of(normalized);

        int selected = SchemaUtil.pickBranchIndex(value, branches, rootSchema);
        if (selected < 0 || selected >= branches.size()) selected = 0;

        String containerId = composedContainerId(uuid, path);
        String collapseId = composedCollapseId(uuid, path);
        String choiceId = composedChoiceId(uuid, path);
        String title = SchemaUtil.schemaDisplayName(schema, path, "Value");

        var choice = select()
                .withId(choiceId)
                .withClass("form-select form-select-sm")
                .withStyle("width: auto; min-width: 10rem;")
                .withName("choice");

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

        JsonObject selectedBranch = branches.get(selected);
        JsonObject selectedResolved = resolveRenderableObjectSchema(selectedBranch, value, rootSchema);
        Tag<?> branchEditor = renderSchemaValue(uuid, path, selectedBranch, value, rootSchema, safeId("field", uuid, path));

        boolean inlinePrimitive = !SchemaUtil.isObjectSchema(selectedResolved)
                && !SchemaUtil.isArraySchema(selectedResolved)
                && !SchemaUtil.hasComposableSchema(selectedResolved);

        Tag<?> body;
        if (inlinePrimitive) {
            body = div().withClass("d-flex align-items-center gap-2 flex-wrap").with(
                    span("Choice").withClass("text-muted small flex-shrink-0"),
                    choice,
                    div().withClass("flex-grow-1 min-w-0").with(branchEditor)
            );
        } else {
            body = div().withClass("d-flex flex-column gap-2").with(
                    div().withClass("d-flex align-items-center gap-2 flex-wrap").with(
                            span("Choice").withClass("text-muted small flex-shrink-0"),
                            choice
                    ),
                    branchEditor
            );
        }

        return renderCollapsibleSection(title, description(normalized), collapseId, "composed-section", body)
                .withId(containerId);
    }

    public static ContainerTag<?> renderPane(String uuid) {
        Asset asset = AssetStore.getAsset(UUID.fromString(uuid));
        if (asset == null) {
            Filament.LOGGER.error("Could not find asset with id {}", uuid);
            throw new IllegalArgumentException();
        }

        String displayName = asset.data.id().toString();
        var regItem = BuiltInRegistries.ITEM.get(asset.data.id());
        if (regItem.isPresent()) {
            var holder = regItem.get();
            List<DomContent> elements = new ArrayList<>();

            var name = asset.data.displayName();
            Component s = name != null ? name : holder.value().getDefaultInstance().getItemName();
            s.visit((style, string) -> {
                if (string.isEmpty()) {
                    return Optional.empty();
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

        JsonElement contentJson = asset.getJson();

        var paneFormContainer = div()
                .withClass("p-0 vh-100 d-flex flex-column overflow-hidden")
                .withId("pane-" + uuid)
                .with(
                        nav().withId("file-navbar").withClass("navbar bg-body-secondary border-bottom border-primary border-2").withStyle("height: 55px;").with(
                                div().withClass("container-fluid d-flex align-items-center h-100").with(
                                        span(rawHtml(displayName)).withClass("navbar-brand mb-0 h1 me-auto"),
                                        div().withClass("position-absolute start-50 translate-middle-x").with(
                                                div().withClass("btn-group").attr("role", "group").with(
                                                        button("Write to file")
                                                                .withId("save-btn")
                                                                .withClass("btn btn-sm btn-outline-primary hint--bottom")
                                                                .attr("hx-get", "/action/save")
                                                                .attr("hx-include", "#current-file-uuid")
                                                                .attr("hx-swap", "none")
                                                                .attr("aria-label", "Write the opened config to file"),

                                                        button("Re-Register")
                                                                .withId("reregister-btn")
                                                                .withClass("btn btn-sm btn-outline-primary hint--bottom hint--large")
                                                                .attr("hx-get", "/action/reregister")
                                                                .attr("hx-include", "#current-file-uuid")
                                                                .attr("hx-swap", "none")
                                                                .attr("aria-label", "This might crash the server, if you have this item in your inventory!")
                                                )
                                        ),

                                        span(asset.isDirty() ? "Unsaved Changes!" : "")
                                                .withId("unsaved-changes")
                                                .withClass("text-warning fw-bold ms-auto")
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
                                    )
                            ),
                            div().withStyle("height: 400px;")
                    )
            );
        }

        paneFormContainer.with(renderCollapsePersistenceScript());
        return paneFormContainer;
    }

    public static ContainerTag<?> renderObjectFieldsContainer(String uuid, String path, JsonObject schema, JsonElement data, JsonObject rootSchema) {
        return div().withId(fieldsContainerId(uuid, path == null ? "" : path)).withClass("object-fields d-flex flex-column gap-1").with(
                buildFieldsFromSchema(schema, data, path == null ? "" : path, uuid, rootSchema)
        );
    }

    private static boolean matchesPatternProperties(JsonObject node, String key) {
        if (node == null || !node.has("patternProperties") || !node.get("patternProperties").isJsonObject()) {
            return false;
        }

        JsonObject patterns = node.getAsJsonObject("patternProperties");
        for (Map.Entry<String, JsonElement> entry : patterns.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            if (key.matches(entry.getKey())) {
                return true;
            }
        }

        return false;
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
                    .withClass("form-label text-wrap mb-0 hint--left hint--no-arrow")
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
        return renderCollapsibleSection(title, desc, objectCollapseId(uuid, path), "object-section", renderObjectFieldsContainer(uuid, path, schema, value, rootSchema));
    }

    private static Tag<?> renderArrayField(JsonObject node, JsonElement data, String path, String uuid, JsonObject rootSchema) {
        String title = SchemaUtil.schemaDisplayName(node, path, "Array");
        String desc = description(node);
        JsonObject itemsSchema = node.has("items") && node.get("items").isJsonObject() ? SchemaUtil.resolveSchema(node.getAsJsonObject("items"), rootSchema) : new JsonObject();
        JsonArray arr = data != null && data.isJsonArray() ? data.getAsJsonArray() : new JsonArray();
        return renderCollapsibleSection(title, desc, arrayCollapseId(uuid, path), "array-section", renderArrayContainer(uuid, path, node, itemsSchema, arr, rootSchema));
    }

    public static ContainerTag<?> renderArrayContainer(String uuid, String path, JsonObject arraySchema, JsonObject itemsSchema, @NotNull JsonArray arr, JsonObject root) {
        String safe = arrayContainerId(uuid, path);
        var container = div().withId(safe).withClass("array-container d-flex flex-column gap-1 p-2 mb-1 border rounded bg-body-tertiary");

        for (int i = 0; i < arr.size(); i++) {
            container.with(renderArrayItemElement(uuid, path, itemsSchema, arr.get(i), i, root));
        }

        Integer maxItems = SchemaUtil.getMaxItems(arraySchema);
        boolean canAdd = maxItems == null || arr.size() < maxItems;

        if (canAdd) {
            container.with(
                    button("+ Add Item")
                            .withType("button")
                            .withClass("btn btn-sm btn-outline-primary w-100 mt-1")
                            .attr("hx-post", WebPaths.fragment(FragmentServlet.Operation.ADD_ARRAY.toString(), uuid, path))
                            .attr("hx-target", "#" + safe)
                            .attr("hx-swap", "outerHTML")
            );
        }

        return container;
    }

    public static Tag<?> renderArrayItemElement(String uuid, String path, JsonObject itemsSchema, JsonElement itemValue, int index, JsonObject root) {
        String itemPrefix = path + "[" + index + "]";
        Tag<?> editor = renderSchemaValue(uuid, itemPrefix, SchemaUtil.normalizeSchema(itemsSchema, root), itemValue, root, safeId("field", uuid, itemPrefix));
        return div().withClass("array-item d-flex align-items-start gap-2 mb-1 pb-1 border-bottom").with(
                div().withClass("flex-grow-1 min-w-0").with(editor),
                renderRemoveButton(uuid, path, String.valueOf(index), arrayContainerId(uuid, path))
        );
    }

    public static ContainerTag<?> renderJsonPreviewFragment(String uuid, JsonElement documentJson, boolean oob) {
        var t = pre().withId("json-preview-" + uuid).withClass("theme-tokyo-night-dark").with(
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
                    .withStyle("min-height: 360px;")
                    .withText("No Preview Available");
            if (oob) return t.attr("hx-swap-oob", true);
            return t;
        }

        Model first = null;
        for (Model model : models) {
            if (model.data() != null && model.data().length > 0) {
                first = model;
                break;
            }
        }

        if (first == null) {
            var t = div()
                    .withId(previewId)
                    .withClass("border rounded bg-body-secondary d-flex align-items-center justify-content-center text-muted")
                    .withStyle("min-height: 360px;")
                    .withText("No Preview Available");
            if (oob) return t.attr("hx-swap-oob", true);
            return t;
        }

        String selectId = previewId + "-select";
        String viewerId = previewId + "-viewer";
        String metaPathId = previewId + "-meta-path";
        String metaContainerId = previewId + "-meta-container";
        String metaIdId = previewId + "-meta-id";

        var select = select()
                .withId(selectId)
                .withClass("form-select form-select-sm");

        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            if (model.data() == null || model.data().length == 0) {
                continue;
            }

            String label = model.path() == null ? "Unknown Path" : model.path();
            String containerPath = model.containerPath() == null ? "Unknown Container" : model.containerPath();
            String modelId = model.id() == null ? "Unknown ID" : model.id();
            String src = "data:model/gltf-binary;base64," + Base64.getEncoder().encodeToString(model.data());

            var option = option(modelId)
                    .withValue(String.valueOf(i))
                    .attr("data-src", src)
                    .attr("data-path", label)
                    .attr("data-container-path", containerPath)
                    .attr("data-model-id", modelId);

            if (model == first) {
                option.attr("selected", "selected");
            }

            select.with(option);
        }

        String firstPath = first.path() == null ? "Unknown Path" : first.path();
        String firstContainerPath = first.containerPath() == null ? "Unknown Container" : first.containerPath();
        String firstModelId = first.id() == null ? "Unknown ID" : first.id();
        String firstSrc = "data:model/gltf-binary;base64," + Base64.getEncoder().encodeToString(first.data());

        var viewer = div()
                .withId(viewerId)
                .withClass("border bg-black overflow-hidden")
                .withStyle("min-height: 300px;")
                .with(
                        rawHtml(String.format(
                                "<model-viewer src=\"%s\" interaction-prompt=\"none\" camera-controls camera-orbit=\"45deg 55deg 4m\" style=\"width:100%%;height:300px; border-radius: 4px;\"></model-viewer>",
                                firstSrc
                        ))
                );

        var meta = div().withClass("d-flex flex-column gap-0 px-1").with(
                div().withId(metaPathId).withClass("text-muted text-truncate w-100").withStyle("font-size: 0.7rem;").withText(firstPath),
                div().withId(metaContainerId).withClass("text-muted text-truncate w-100").withStyle("font-size: 0.7rem;").withText(firstContainerPath),
                div().withId(metaIdId).withClass("text-muted text-truncate w-100").withStyle("font-size: 0.7rem;").withText(firstModelId)
        );

        var root = div()
                .withId(previewId)
                .withClass("d-flex flex-column gap-2")
                .with(
                        select,
                        meta,
                        viewer,
                        renderModelViewerScript(previewId, selectId, viewerId, metaPathId, metaContainerId, metaIdId)
                );

        if (oob) return root.attr("hx-swap-oob", true);
        return root;
    }

    private static ContainerTag<?> renderModelViewerScript(
            String previewId,
            String selectId,
            String viewerId,
            String metaPathId,
            String metaContainerId,
            String metaIdId
    ) {
        // todo: this should be a file somewhere
        return script(rawHtml("""
            (function () {
              function init(root) {
                const scope = root || document;
                const container = scope.querySelector('#%s');
                if (!container || container.dataset.viewerBound === '1') return;
                container.dataset.viewerBound = '1';
            
                const select = container.querySelector('#%s');
                const viewerHost = container.querySelector('#%s');
                const pathEl = container.querySelector('#%s');
                const containerEl = container.querySelector('#%s');
                const idEl = container.querySelector('#%s');
            
                if (!select || !viewerHost) return;
            
                function setViewerFromOption(option) {
                  if (!option) return;
            
                  const src = option.getAttribute('data-src') || '';
                  const path = option.getAttribute('data-path') || 'Unknown Path';
                  const containerPath = option.getAttribute('data-container-path') || 'Unknown Container';
                  const modelId = option.getAttribute('data-model-id') || 'Unknown ID';
            
                  viewerHost.innerHTML = '';
            
                  const viewer = document.createElement('model-viewer');
                  viewer.setAttribute('interaction-prompt', 'none');
                  viewer.setAttribute('camera-controls', '');
                  viewer.setAttribute('camera-orbit', '45deg 55deg 4m');
                  viewer.style.width = '100%%';
                  viewer.style.height = '300px';
                  viewer.style.borderRadius = '4px';
                  viewer.src = src;
            
                  viewerHost.appendChild(viewer);
            
                  if (pathEl) pathEl.textContent = path;
                  if (containerEl) containerEl.textContent = containerPath;
                  if (idEl) idEl.textContent = modelId;
                }
            
                select.addEventListener('change', function () {
                  setViewerFromOption(select.selectedOptions[0]);
                });
            
                setViewerFromOption(select.selectedOptions[0]);
              }
            
              init(document);
            
              document.addEventListener('DOMContentLoaded', function () { init(document); });
              document.addEventListener('htmx:afterSwap', function () { init(document); });
            })();
            """.formatted(previewId, selectId, viewerId, metaPathId, metaContainerId, metaIdId)));
    }

    record Model(String path, String containerPath, String id, String assetPath, byte[] data) {

    }

    private static @NotNull List<Model> resolveModel(Asset asset) {
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
                    if (source == null)
                        itemModel = asset.data.vanillaItem().getDefaultInstance().get(DataComponents.ITEM_MODEL);
                }

            }

            String type = "item_resource";
            if (asset.data instanceof ItemData id) {
                res = id.itemResource();
            } else if (asset.data instanceof BlockData bd) {
                res = bd.blockResource() != null ? bd.blockResource() : bd.itemResource();
                type = "item_resource/block_resource";
            } else if (asset.data instanceof DecorationData dd) {
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

        } catch (Exception ignored) {
            Filament.LOGGER.error("Error while collecting models for preview for {}", asset.data.id());
        }

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
                    } catch (Exception ignored) {
                    }
                }

                collectModels(itemModel, value, newPath, models);
            }
        } else if (el.isJsonArray()) {
            int i = 0;
            for (JsonElement child : el.getAsJsonArray()) {
                collectModels(itemModel, child, path + "[" + i++ + "]", models);
            }
        }
    }
}
