package de.tomalbrc.filament.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.resource.ResourceProvider;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.ItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.SelectItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.select.CustomModelDataStringProperty;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.DyeTintSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TransparentModelGenerator {
    private TransparentModelGenerator() {}

    public static final float PREVIEW_OPACITY = 0.5f;
    public static final String PREVIEW_SUFFIX = "_preview";

    private static final int MAX_PARENT_DEPTH = 32;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void createPreviewVariants(ResourcePackBuilder builder, Identifier id, ResourceProvider resourceProvider, boolean tint) {
        Map<String, Identifier> models = resourceProvider.getModels();
        if (models.isEmpty()) {
            Filament.LOGGER.error("Cannot generate preview for {}: no models", id);
            return;
        }

        String defaultKey = models.containsKey("default")
                ? "default"
                : models.keySet().iterator().next();

        List<SelectItemModel.Case<String>> cases = new ObjectArrayList<>();

        for (Map.Entry<String, Identifier> entry : models.entrySet()) {
            if (entry.getKey().equals("default")) continue;
            ItemModel model = new BasicItemModel(
                    entry.getValue(),
                    !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFFFF))
            );
            cases.add(new SelectItemModel.Case<>(List.of(entry.getKey()), model));
        }

        Map<String, Identifier> previewModels = new LinkedHashMap<>();

        for (Map.Entry<String, Identifier> entry : models.entrySet()) {
            String key = entry.getKey();
            Identifier originalModelId = entry.getValue();

            Identifier previewModelId = generateTransparentModel(builder, originalModelId);
            if (previewModelId == null) {
                Filament.LOGGER.warn("Skipping preview for {} (transparent model generation failed)", originalModelId);
                continue;
            }

            previewModels.put(key, previewModelId);

            ItemModel previewModel = new BasicItemModel(
                    previewModelId,
                    !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFFFF))
            );

            cases.add(new SelectItemModel.Case<>(List.of(key + PREVIEW_SUFFIX), previewModel));
        }

        if (previewModels.isEmpty()) {
            Filament.LOGGER.error("No preview cases generated for {}", id);
            return;
        }

        Identifier fallbackId = previewModels.getOrDefault(
                defaultKey,
                previewModels.values().iterator().next()
        );

        ItemModel fallbackModel = new BasicItemModel(
                fallbackId,
                !tint ? List.of() : List.of(new DyeTintSource(0xFFFFFFFF))
        );

        builder.addData(AssetPaths.itemAsset(id), new ItemAsset(
                new SelectItemModel<>(
                        new SelectItemModel.Switch<>(
                                new CustomModelDataStringProperty(0),
                                cases
                        ),
                        Optional.of(fallbackModel),
                        Optional.empty()
                ),
                ItemAsset.Properties.DEFAULT
        ).toBytes());
    }

    private static Identifier generateTransparentModel(ResourcePackBuilder builder, Identifier originalModelId) {
        byte[] modelData = builder.getDataOrSource(AssetPaths.model(originalModelId) + ".json");
        if (modelData == null) {
            Filament.LOGGER.warn("Model not found in pack or source: {}", AssetPaths.model(originalModelId) + ".json");
            return null;
        }

        JsonObject originalModel;
        try {
            originalModel = JsonParser.parseString(new String(modelData, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            Filament.LOGGER.warn("Invalid JSON in model {}: {}", originalModelId, e.getMessage());
            return null;
        }

        ResolvedModel resolved = new ResolvedModel();
        resolveTexturesInto(builder, originalModelId, resolved, 0);

        if (resolved.textures.isEmpty()) {
            Filament.LOGGER.warn("Model {} has no resolvable textures", originalModelId);
            return null;
        }

        Map<String, String> transparentTextures = new LinkedHashMap<>();
        boolean anyWritten = false;

        for (Map.Entry<String, String> entry : resolved.textures.entrySet()) {
            String slot = entry.getKey();
            String ref = resolveTextureRef(entry.getValue(), resolved.textures);

            if (ref == null) {
                Filament.LOGGER.warn("Unresolvable texture ref '{}' for {}", entry.getValue(), originalModelId);
                transparentTextures.put(slot, entry.getValue());
                continue;
            }

            Identifier textureId = tryParseTextureId(ref);
            if (textureId == null) {
                transparentTextures.put(slot, entry.getValue());
                continue;
            }

            Identifier transparentId = makeTransparentTexture(builder, textureId);
            if (transparentId == null) {
                transparentTextures.put(slot, entry.getValue());
                continue;
            }

            transparentTextures.put(slot, transparentId.toString());
            anyWritten = true;
        }

        if (!anyWritten) {
            Filament.LOGGER.warn("No textures could be made transparent for {}", originalModelId);
            return null;
        }

        JsonObject newModel = new JsonObject();
        newModel.addProperty("parent", originalModel.has("parent")
                ? originalModel.get("parent").getAsString()
                : "minecraft:item/generated");

        JsonObject texturesObj = new JsonObject();
        for (Map.Entry<String, String> entry : transparentTextures.entrySet()) {
            texturesObj.addProperty(entry.getKey(), entry.getValue());
        }
        newModel.add("textures", texturesObj);

        Identifier newModelId = Identifier.fromNamespaceAndPath(
                originalModelId.getNamespace(),
                originalModelId.getPath() + PREVIEW_SUFFIX
        );

        builder.addData(AssetPaths.model(newModelId),
                GSON.toJson(newModel).getBytes(StandardCharsets.UTF_8));

        return newModelId;
    }

    private static void resolveTexturesInto(ResourcePackBuilder builder, Identifier modelId, ResolvedModel out, int depth) {
        if (depth > MAX_PARENT_DEPTH) {
            Filament.LOGGER.warn("Model parent chain too deep at {}", modelId);
            return;
        }

        byte[] data = builder.getDataOrSource(AssetPaths.model(modelId));
        if (data == null) return;

        JsonObject json;
        try {
            json = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return;
        }

        if (json.has("parent")) {
            Identifier parentId = Identifier.tryParse(json.get("parent").getAsString());
            if (parentId != null) {
                resolveTexturesInto(builder, parentId, out, depth + 1);
            }
        }

        if (json.has("textures")) {
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("textures").entrySet()) {
                out.textures.put(e.getKey(), e.getValue().getAsString());
            }
        }

        if (json.has("render_type") && out.renderType == null) {
            out.renderType = json.get("render_type").getAsString();
        }
    }

    private static String resolveTextureRef(String ref, Map<String, String> allTextures) {
        if (!ref.startsWith("#")) return ref;

        Set<String> visited = new HashSet<>();
        while (ref.startsWith("#")) {
            if (!visited.add(ref)) return null;
            String next = allTextures.get(ref.substring(1));
            if (next == null) return null;
            ref = next;
        }
        return ref;
    }

    private static Identifier tryParseTextureId(String ref) {
        if (ref.startsWith("#")) return null;
        try {
            return Identifier.tryParse(ref);
        } catch (Exception e) {
            return null;
        }
    }

    private static Identifier makeTransparentTexture(ResourcePackBuilder builder, Identifier textureId) {
        byte[] pngBytes = builder.getDataOrSource(AssetPaths.texture(textureId));
        if (pngBytes == null) {
            Filament.LOGGER.warn("Texture not found in pack or source: {}", textureId);
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
            if (image == null) {
                Filament.LOGGER.warn("Cannot decode texture: {}", textureId);
                return null;
            }

            BufferedImage transparent = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int newAlpha = Math.round(alpha * PREVIEW_OPACITY);
                    transparent.setRGB(x, y, (newAlpha << 24) | (argb & 0x00FFFFFF));
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(transparent, "PNG", out);

            Identifier newTextureId = Identifier.fromNamespaceAndPath(
                    textureId.getNamespace(),
                    textureId.getPath() + PREVIEW_SUFFIX
            );

            builder.addData(AssetPaths.texture(newTextureId), out.toByteArray());
            return newTextureId;

        } catch (IOException e) {
            Filament.LOGGER.error("Failed to process texture {}: {}", textureId, e.getMessage());
            return null;
        }
    }

    private static final class ResolvedModel {
        final Map<String, String> textures = new LinkedHashMap<>(); // TODO: sprite object entry support
        String renderType;
    }
}