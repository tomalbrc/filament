package de.tomalbrc.filament.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Seat;
import de.tomalbrc.filament.behaviour.item.*;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.io.FilenameUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

        Map<String, String> redirects = new Object2ObjectOpenHashMap<>();
        try (var stream = Files.list(root)) {
            stream.forEach(x -> importPack(x, redirects));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void importPack(Path path, Map<String, String> fileRedirects) {
        String dirName = FilenameUtils.getBaseName(path.toString());

        try (var stream = Files.walk(path.resolve("items"))) {
            stream.forEach(file -> {
                try {
                    Path filename = file.toAbsolutePath();
                    String ext = FilenameUtils.getExtension(filename.toString());
                    String baseName = FilenameUtils.getBaseName(filename.toString());
                    if (baseName.startsWith("."))
                        return;

                    if (ext != null && (ext.equals("yml") || ext.equals("yaml"))) {
                        InputStream inputStream = new FileInputStream(file.toFile());
                        importSingleFile(dirName, inputStream, fileRedirects);
                    }
                } catch (Throwable e) {
                    Filament.LOGGER.error("Error loading nexo file", e);
                }
            });
        } catch (Throwable e) {
            Filament.LOGGER.error("Error reading nexo directory", e);
        }

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            Set<ResourceLocation> texturePaths = new ObjectArraySet<>();

            Path packPath = path.resolve("pack");
            try (var walk = Files.walk(packPath)) {
                walk.forEach(filepath -> {
                    if (filepath.toFile().isDirectory())
                        return;

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

                        resourcePackBuilder.addData(fileRedirects.getOrDefault(relativePath, relativePath), stream.readAllBytes());
                    } catch (Throwable e) {
                        Filament.LOGGER.error("Error reading nexo asset", e);
                    }
                });
            } catch (Throwable e) {
                Filament.LOGGER.error("Error reading nexo pack assets", e);
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

    public static void importSingleFile(String baseName, InputStream inputStream, Map<String, String> fileRedirects) {
        Yaml yaml = new Yaml();
        Map<String, Object> elements = yaml.load(inputStream);
        for (Map.Entry<String, Object> element : elements.entrySet()) {
            processElement(ResourceLocation.fromNamespaceAndPath(baseName, element.getKey()), element.getValue(), fileRedirects);
        }
    }

    private static void processElement(ResourceLocation id, Object data, Map<String, String> fileRedirects) {
        var name = getValue("displayname", data, String.class);
        if (name == null)
            name = getValue("itemname", data, String.class); // fallback for older configs (nexo for <1.20.4)

        var builder = DataComponentMap.builder();
        if (name != null)
            builder.set(DataComponents.ITEM_NAME, TextParserUtils.formatText(name));

        var material = getValue("material", data, String.class);
        Item vanillaItem = null;
        if (material != null) {
            vanillaItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(material.toLowerCase()));
        } else {
            vanillaItem = Items.LEATHER_HORSE_ARMOR;
        }

        var attr = getValue("AttributeModifiers", data, List.class);
        if (attr != null) {
            var attrBuilder = ItemAttributeModifiers.builder();
            for (Object o : attr) {
                var amount = getValue("amount", o, Number.class);
                var attribute = getValue("attribute", o, String.class);
                var operation = getValue("operation", o, Integer.class);
                var slot = EquipmentSlot.valueOf(getValue("slot", o, String.class));

                attrBuilder.add(BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attribute.toLowerCase().replace("_", "."))).orElseThrow(), new AttributeModifier(ResourceLocation.fromNamespaceAndPath("filament", "armor"), amount.doubleValue(), Arrays.stream(AttributeModifier.Operation.values()).filter(y -> y.id() == operation).findAny().orElseThrow()), EquipmentSlotGroup.bySlot(slot));
            }
            builder.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        }

        var mechanics = getMap("Mechanics", data);
        Map<String, Object> customBlock = null;
        Map<String, Object> furniture = null;
        Integer dur = null;
        if (mechanics != null) {
            customBlock = getMap("custom_block", mechanics);
            furniture = getMap("furniture", mechanics);
            dur = getValue("durability", mechanics, Integer.class);
        }

        if (dur != null) builder.set(DataComponents.MAX_DAMAGE, dur);

        if (customBlock != null || furniture != null) {
            if (customBlock != null) {
                // load as block
                var model = getValue("model", data, String.class);
                var props = new BlockProperties();
                props.copyComponents = true;
                props.destroyTime = 2;
                props.explosionResistance = 2;
                props.solid = true;
                props.transparent = false;
                props.allowsSpawning = true;

                var lightObj = getValue("light", furniture, Integer.class);
                if (lightObj != null)
                    props.lightEmission = BlockStateMappedProperty.of(lightObj);

                BlockData blockData = new BlockData(
                        id,
                        vanillaItem,
                        null,
                        new BlockResource(Map.of("default", new PolymerBlockModel(ResourceLocation.parse(model), 0, 0, false, 0))),
                        null,
                        BlockStateMappedProperty.of(BlockModelType.FULL_BLOCK),
                        props,
                        null,
                        builder.build(),
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
                props.copyComponents = true;
                props.destroyTime = 0.5f;
                props.explosionResistance = 0.5f;
                props.solid = false;
                props.transparent = true;
                props.allowsSpawning = false;
                var rotObj = getValue("rotatable", furniture, Boolean.class);
                props.rotate = rotObj == Boolean.TRUE || rotObj == null;

                var lightObj = getValue("light", furniture, Integer.class);
                if (lightObj != null)
                    props.lightEmission = BlockStateMappedProperty.of(lightObj);

                var restrictedRot = getValue("restricted_rotation", furniture, String.class);
                if (restrictedRot != null) {
                    props.rotateSmooth = restrictedRot.equals("STRICT");
                }

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
                if (barrier != null && barrier) {
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
                            seatConf.offset = new Vector3f(blockConfig.origin().add(0, 0.5f + height, 0, new Vector3f()));
                            seatConf.direction = yaw;
                            filamentSeats.add(seatConf);
                        }
                    }
                }

                if (!filamentSeats.isEmpty()) {
                    behaviourConfigMap.put(Behaviours.SEAT, filamentSeats);
                }

                DecorationData decorationData = new DecorationData(
                        id,
                        null,
                        ItemResource.of(Map.of("default", ResourceLocation.parse(model)), null, null),
                        vanillaItem,
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
            }
        } else {
            BehaviourConfigMap behaviourConfigMap = new BehaviourConfigMap();

            // load as simple item
            var pack = getMap("Pack", data);
            var model = getValue("model", pack, String.class);
            //if (model == null)
            //    return;

            var props = new ItemProperties();
            props.copyComponents = true;
            props.copyTags = true;

            String parent_model = getValue("parent_model", pack, String.class);
            String texture = getValue("texture", pack, String.class);
            List<String> textureList = getValue("textures", pack, List.class);
            Map<String, ResourceLocation> textures = new Object2ObjectOpenHashMap<>();
            if (parent_model != null) {
                if (texture != null) {
                    textures.put("layer0", ResourceLocation.parse(texture));
                } else if (textureList != null) {
                    for (int i = 0; i < textureList.size(); i++) {
                        textures.put("layer" + i, ResourceLocation.parse(textureList.get(i)));
                    }
                }
            }

            var customArmor = getMap("#CustomArmor", pack);
            if (customArmor != null) {
                String l1 = customArmor.get("layer1").toString();
                String l2 = customArmor.get("layer2").toString();
                var src1 = "assets/minecraft/textures/" + l1 + ".png";
                var src2 = "assets/minecraft/textures/" + l2 + ".png";
                var pathParts = l1.replace("_layer_1", "").split("/");
                var path = pathParts[pathParts.length - 1];
                fileRedirects.put(src1, "assets/minecraft/textures/trims/models/armor/" + path + ".png");
                fileRedirects.put(src2, "assets/minecraft/textures/trims/models/armor/" + path + "_leggings.png");

                var conf = new Armor.Config();
                conf.trim = true;
                conf.texture = ResourceLocation.withDefaultNamespace(path);
                if (vanillaItem instanceof ArmorItem armorItem) {
                    conf.slot = armorItem.getEquipmentSlot();
                }

                behaviourConfigMap.put(Behaviours.ARMOR, conf);
            }

            if (vanillaItem instanceof ElytraItem) {
                behaviourConfigMap.put(Behaviours.ELYTRA, new Elytra.Config());
            }

            if (vanillaItem instanceof ShovelItem) {
                behaviourConfigMap.put(Behaviours.SHOVEL, new Shovel.Config());
            }

            if (vanillaItem instanceof HoeItem) {
                behaviourConfigMap.put(Behaviours.HOE, new Hoe.Config());
            }

            if (vanillaItem instanceof ShearsItem) {
                behaviourConfigMap.put(Behaviours.SHEARS, new Shears.Config());
            }

            if (vanillaItem instanceof AxeItem) {
                behaviourConfigMap.put(Behaviours.STRIPPER, new Stripper.Config());
            }

            if (mechanics != null && mechanics.containsKey("cosmetic") && vanillaItem instanceof Equipable equipable) {
                var conf = new Cosmetic.Config();
                conf.slot = equipable.getEquipmentSlot();
                behaviourConfigMap.put(Behaviours.COSMETIC, conf);
            }

            ItemData itemData = new ItemData(
                    id,
                    vanillaItem,
                    null,
                    ItemResource.of(model == null ? null : new Object2ObjectArrayMap<>(new String[]{"default"}, new ResourceLocation[]{ResourceLocation.parse(model)}), parent_model == null ? null : ResourceLocation.parse(parent_model), textures.isEmpty() ? null : Map.of("default", textures)),
                    behaviourConfigMap,
                    props,
                    builder.build(),
                    null,
                    null
            );

            ItemRegistry.register(itemData);
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
