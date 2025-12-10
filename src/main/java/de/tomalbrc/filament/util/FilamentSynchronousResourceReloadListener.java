package de.tomalbrc.filament.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.registry.Templates;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface FilamentSynchronousResourceReloadListener extends SimpleSynchronousResourceReloadListener {
    default void loadJson(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<Identifier, InputStream> onRead) {
        var resources = resourceManager.listResources(root, path -> path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (InputStream inputStream = entry.getValue().open()) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                var json = JsonParser.parseReader(new InputStreamReader(inputStream));
                InputStream stream;
                if (json.isJsonObject()) {
                    Type mapType = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    Map<String, Object> document = gson.fromJson(json, mapType);
                    if (document != null) {
                        document = Json.camelToSnakeCase(document);
                    }
                    stream = new ByteArrayInputStream(gson.toJson(document).getBytes(StandardCharsets.UTF_8));
                }
                else {
                    stream = new ByteArrayInputStream(gson.toJson(json).getBytes(StandardCharsets.UTF_8));
                }

                onRead.accept(entry.getKey(), stream);
            } catch (IOException | IllegalStateException e) {
                error(entry.getKey(), e);
            }
        }
    }

    default void loadYaml(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<Identifier, InputStream> onRead) {
        var resources = resourceManager.listResources(root, path ->
                path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".yaml") ||
                        path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".yml"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (InputStream inputStream = entry.getValue().open()) {
                var list = Json.yamlToJson(inputStream);
                for (InputStream stream : list) {
                    onRead.accept(entry.getKey(), stream);
                }
            } catch (IOException | IllegalStateException e) {
                error(entry.getKey(), e);
            }
        }
    }

    default void load(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<Identifier, InputStream> onRead) {
        loadYaml(root, endsWith, resourceManager, (id, input) -> {
            if (!loadAsTemplate(input))
                onRead.accept(id, template(input));
        });

        loadJson(root, endsWith, resourceManager, (id, input) -> {
            if (!loadAsTemplate(input))
                onRead.accept(id, template(input));
        });
    }

    static boolean loadAsTemplate(InputStream input) {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(input));
        if (element != null && element.isJsonObject()) {
            try {
                if (element.getAsJsonObject().has("is_template") && element.getAsJsonObject().getAsJsonPrimitive("is_template").getAsBoolean()) {
                    Templates.add(input);
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    static void error(Identifier Identifier, Exception e) {
        Filament.LOGGER.error("Failed to load resource \"{}\".", Identifier, e);
    }

    static InputStream template(InputStream inputStream) {
        var parsed = JsonParser.parseReader(new InputStreamReader(inputStream));
        try {
            inputStream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!parsed.isJsonObject() || !parsed.getAsJsonObject().has("id"))
            return inputStream;

        var parsedObject = parsed.getAsJsonObject();
        var realId = Identifier.parse(parsedObject.getAsJsonPrimitive("id").getAsString());
        var multiTemp = parsedObject.has("templates");
        var singleTemp = parsedObject.has("template");
        if (multiTemp || singleTemp) {
            List<Identifier> templates = new ArrayList<>();
            if (multiTemp) {
                JsonElement templateEl = parsedObject.get("templates");
                if (templateEl.isJsonArray()) {
                    JsonArray array = templateEl.getAsJsonArray();
                    for (JsonElement element : array) {
                        if (element.isJsonPrimitive()) {
                            templates.add(Identifier.parse(element.getAsJsonPrimitive().getAsString()));
                        }
                    }
                }
            }
            else {
                JsonElement templateEl = parsedObject.get("template");
                if (templateEl.isJsonPrimitive()) {
                    templates.add(Identifier.parse(templateEl.getAsString()));
                }
            }

            for (Identifier template : templates) {
                parsed = Templates.merge(template, realId, parsed.getAsJsonObject());
            }

            parsed.getAsJsonObject().remove("is_template");

            JsonElement res = Templates.handlePlaceholder(parsed, realId);

            String jsonString = new Gson().toJson(res);
            return new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        }

        return inputStream;
    }
}
