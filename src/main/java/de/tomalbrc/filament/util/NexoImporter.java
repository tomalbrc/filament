package de.tomalbrc.filament.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Seat;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.properties.DecorationProperties;
import de.tomalbrc.filament.data.properties.ItemProperties;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.apache.commons.compress.utils.FileNameUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NexoImporter {
    public static void importAll() {
        var root = FabricLoader.getInstance().getGameDir().resolve("nexo");
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (var stream = Files.list(root)) {
            stream.forEach(NexoImporter::importPack);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void importPack(Path path) {
        String dirName = FileNameUtils.getBaseName(path);

        try (var stream = Files.walk(path.resolve("items"))) {
            stream.forEach(file -> {
                try {
                    Path filename = file.toAbsolutePath();
                    String ext = FileNameUtils.getExtension(filename);
                    String baseName = FileNameUtils.getBaseName(filename);
                    if (baseName.startsWith("."))
                        return;

                    if (ext != null && (ext.equals("yml") || ext.equals("yaml"))) {
                        InputStream inputStream = new FileInputStream(file.toFile());
                        importSingleFile(dirName, inputStream);
                    }
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            Set<ResourceLocation> texturePaths = new ObjectArraySet<>();

            Path packPath = path.resolve("pack");
            try (var walk = Files.walk(packPath)) {
                walk.forEach(filepath -> {
                    try (var stream = new FileInputStream(filepath.toFile())) {
                        String relativePath = packPath.relativize(filepath).toString().replace("\\", "/");

                        // oraxen or older packs
                        if (!relativePath.startsWith("assets/")) {
                            relativePath = "assets/minecraft/" + relativePath;
                        }

                        String dir = getTextureParent(relativePath);
                        if (dir != null && relativePath.endsWith(".png")) {
                            String ns = getNamespace(relativePath);
                            if (ns != null) {
                                texturePaths.add(ResourceLocation.fromNamespaceAndPath(ns, dir));
                            }
                        }

                        resourcePackBuilder.addData(relativePath, stream.readAllBytes());
                    } catch (Throwable ignored) {
                        ignored.printStackTrace();
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }

            byte[] atlas = generateAtlasJson(texturePaths);
            resourcePackBuilder.addData("assets/minecraft/atlases/blocks.json", atlas);
            resourcePackBuilder.addData("assets/minecraft/atlases/particle.json", atlas);
        });
    }

    public static String getTextureParent(String fullPath) {
        String normalized = fullPath.replace("\\", "/"); // windoof
        String prefix = "assets/";
        String texturesToken = "/textures/";

        int assetsIndex = normalized.indexOf(prefix);
        if (assetsIndex == -1) return null;

        int texturesIndex = normalized.indexOf(texturesToken, assetsIndex);
        if (texturesIndex == -1) return null;

        String afterTextures = normalized.substring(texturesIndex + texturesToken.length());

        int lastSlash = afterTextures.lastIndexOf('/');
        if (lastSlash == -1) return "";

        return afterTextures.substring(0, lastSlash);
    }

    public static String getNamespace(String fullPath) {
        String normalized = fullPath.replace("\\", "/");
        String prefix = "assets/";
        String texturesToken = "/textures/";

        int assetsIndex = normalized.indexOf(prefix);
        int texturesIndex = normalized.indexOf(texturesToken, assetsIndex);

        if (assetsIndex == -1 || texturesIndex == -1) return null;

        return normalized.substring(assetsIndex + prefix.length(), texturesIndex);
    }

    private static byte[] generateAtlasJson(Collection<ResourceLocation> sourceDirs) {
        JsonObject root = new JsonObject();
        JsonArray sources = new JsonArray();

        for (ResourceLocation dir : sourceDirs) {
            JsonObject source = new JsonObject();
            source.addProperty("type", "directory");
            source.addProperty("prefix", dir.getPath() + "/");
            source.addProperty("source", dir.getPath());
            sources.add(source);
        }
        root.add("sources", sources);

        Gson gson = new GsonBuilder().create();
        return gson.toJson(root).getBytes(StandardCharsets.UTF_8);
    }

    public static void importSingleFile(String baseName, InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> elements = yaml.load(inputStream);
        for (Map.Entry<String, Object> element : elements.entrySet()) {
            processElement(ResourceLocation.fromNamespaceAndPath(baseName, element.getKey()), element.getValue());
        }
    }

    private static void processElement(ResourceLocation id, Object data) {
        var name = getValue("displayname", data, String.class);
        if (name == null) name = getValue("itemname", data, String.class); // fallback for older configs (nexo for <1.20.4)

        var material = getValue("material", data, String.class);

        var mechanics = getMap("Mechanics", data);
        if (mechanics != null) {
            var customBlock = getMap("custom_block", mechanics);
            var furniture = getMap("furniture", mechanics);
            if (customBlock != null) {
                // load as block
                var model = getValue("model", data, String.class);

                var props = new BlockProperties();
                props.destroyTime = 2;
                props.explosionResistance = 2;
                props.solid = true;
                props.transparent = false;
                props.allowsSpawning = true;

                BlockData blockData = new BlockData(
                        id,
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(material.toLowerCase())),
                        Map.of("en_us", name),
                        new BlockResource(Map.of("default", new PolymerBlockModel(ResourceLocation.parse(model), 0, 0, false, 0))),
                        null,
                        BlockStateMappedProperty.of(BlockModelType.FULL_BLOCK),
                        props,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                BlockRegistry.register(blockData);

            } else if (furniture != null) {
                // load as decoration
                var pack = getMap("Pack", data);
                var model = getValue("model", pack, String.class);

                if (model == null)
                    return;

                var props = new DecorationProperties();
                props.destroyTime = 2;
                props.explosionResistance = 2;
                props.solid = false;
                props.transparent = true;
                props.allowsSpawning = false;
                var rotObj = getValue("rotatable", furniture, Boolean.class);
                props.rotate = rotObj == Boolean.TRUE || rotObj == null;

                var placing = getMap("limited_placing", furniture);
                if (placing != null) {
                    props.placement = new DecorationProperties.Placement(
                            getValue("wall", placing, Boolean.class) == Boolean.TRUE,
                            getValue("floor", placing, Boolean.class) == Boolean.TRUE,
                            getValue("roof", placing, Boolean.class) == Boolean.TRUE);
                }

                var drop = getMap("drop", furniture);
                if (drop != null) {
                    // TODO silk_touch
                    // and custom drop support
                    props.drops = true;
                } else {
                    props.drops = false;
                }

                var type = getValue("type", furniture, String.class);
                boolean frame = false;
                if (type != null && type.endsWith("ITEM_FRAME")) {
                    props.display = ItemDisplayContext.FIXED;
                    frame = true;
                    if (type.startsWith("GLOW")) {
                        props.glow = true;
                    }
                }

                var barrier = getValue("barrier", furniture, Boolean.class);
                List<DecorationData.BlockConfig> blocks = new ObjectArrayList<>();
                if (barrier != null) {
                    blocks.add(new DecorationData.BlockConfig(new Vector3f(), new Vector3f(1)));
                }

                var barriers = getValue("barriers", furniture, List.class);
                if (barriers != null) {
                    for (Object blockConf : barriers) {
                        boolean origin = blockConf instanceof String s && s.equals("origin");
                        if (origin) {
                            blocks.add(new DecorationData.BlockConfig(new Vector3f(), new Vector3f(1)));
                        } else {
                            Integer x = getValue("x", blockConf, Integer.class);
                            Integer y = getValue("y", blockConf, Integer.class);
                            Integer z = getValue("z", blockConf, Integer.class);
                            blocks.add(new DecorationData.BlockConfig(new Vector3f(
                                    x == null ? 0 : x,
                                    y == null ? 0 : y,
                                    z == null ? 0 : z
                            ).rotateY(Mth.PI).round(), new Vector3f(1)));
                        }
                    }
                }

                BehaviourConfigMap behaviourConfigMap = new BehaviourConfigMap();

                Seat.SeatConfig filamentSeats = new Seat.SeatConfig();
                var seat = getMap("seat", furniture);
                if (seat != null) {
                    var height = getValue("height", seat, Float.class);
                    var yaw = getValue("yaw", seat, Float.class);

                    if (yaw == null) yaw = 180f;
                    else yaw -= 180;

                    if (height == null) height = 0f;

                    if (blocks.isEmpty()) {
                        var seatConf = new Seat.SeatConfigData();
                        seatConf.offset = new Vector3f();
                        filamentSeats.add(seatConf);
                    } else {
                        for (DecorationData.BlockConfig blockConfig : blocks) {
                            var seatConf = new Seat.SeatConfigData();
                            seatConf.offset = new Vector3f(blockConfig.origin().add(0, 0.25f + height, 0, new Vector3f()));
                            seatConf.direction = yaw;
                            filamentSeats.add(seatConf);
                        }
                    }
                }

                if (!filamentSeats.isEmpty()) {
                    behaviourConfigMap.put(Behaviours.SEAT, filamentSeats);
                }

                var builder = DataComponentMap.builder();
                builder.set(DataComponents.ITEM_NAME, TextParserUtils.formatText(name));

                DecorationData decorationData = new DecorationData(
                        id,
                        null,
                        ItemResource.of(Map.of("default", ResourceLocation.parse(model)), null, null),
                        null,
                        null,
                        blocks.isEmpty() ? null : blocks,
                        blocks.isEmpty() ? new Vector2f(1, 1) : null,
                        props,
                        behaviourConfigMap,
                        builder.build(),
                        null,
                        null,
                        null
                );

                DecorationRegistry.register(decorationData);
            } else {
                // load as simple item
                var pack = getMap("Pack", data);
                var model = getValue("model", pack, String.class);
                if (model == null)
                    return;

                var props = new ItemProperties();

                ItemData itemData = new ItemData(
                        id,
                        null,
                        Map.of("en_us", name),
                        ItemResource.of(Map.of("default", ResourceLocation.parse(model)), null, null),
                        null,
                        props,
                        null,
                        null,
                        null
                );

                ItemRegistry.register(itemData);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(String key, Object obj) {
        if (obj instanceof Map<?, ?> map) {
            Object value = map.get(key);
            if (value instanceof Map<?, ?>) {
                return (Map<String, Object>) value;
            }
        }
        return null;
    }

    public static <T> T getValue(String key, Object obj, Class<T> clazz) {
        if (obj instanceof Map<?, ?> map) {
            Object value = map.get(key);
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
        }
        return null;
    }
}
