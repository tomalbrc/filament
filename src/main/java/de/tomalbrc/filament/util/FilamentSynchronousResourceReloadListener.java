package de.tomalbrc.filament.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Map;
import java.util.function.BiConsumer;

public interface FilamentSynchronousResourceReloadListener extends SimpleSynchronousResourceReloadListener {
    default void loadJson(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<ResourceLocation, InputStream> onRead) {
        var resources = resourceManager.listResources(root, path -> path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
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

    default void loadYaml(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<ResourceLocation, InputStream> onRead) {
        var resources = resourceManager.listResources(root, path ->
                path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".yaml") ||
                        path.getPath().endsWith((endsWith == null ? "" : endsWith) + ".yml"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
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

    default void load(@NotNull String root, @Nullable String endsWith, @NotNull ResourceManager resourceManager, @NotNull BiConsumer<ResourceLocation, InputStream> onRead) {
        loadYaml(root, endsWith, resourceManager, onRead);
        loadJson(root, endsWith, resourceManager, onRead);
    }

    default void error(ResourceLocation resourceLocation, Exception e) {
        Filament.LOGGER.error("Failed to load resource \"{}\".", resourceLocation, e);
    }
}
