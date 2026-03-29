package de.tomalbrc.filamentweb.asset;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.annotation.Description;
import de.tomalbrc.filamentweb.util.PojoComponents;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AssetStore {
    public static byte[] DEFAULT_MODEL;

    static Map<UUID, Asset> assetsByUuid = new ConcurrentHashMap<>();
    static Map<Path, Asset> assetsByPath = new ConcurrentHashMap<>();

    public static void registerAsset(Asset asset) {
        if (asset.schema == null) {
            asset.schema = getSchema(asset.data, asset.type);
        }

        if (asset.path == null || assetsByPath.containsKey(asset.path))
            return;

        assetsByUuid.put(asset.uuid, asset);
        assetsByPath.put(asset.path, asset);
    }

    public static Asset getAsset(UUID uuid) {
        return assetsByUuid.get(uuid);
    }

    public static Map<UUID, Asset> getAssetsByUuid() {
        return assetsByUuid;
    }

    public static void registerAssetFromPath(Data<?> data, Class<?> clazz, Object instance) {
        Asset asset = new Asset();
        asset.uuid = UUID.randomUUID();
        asset.data = data;
        asset.path = data.filepath;
        asset.type = clazz;
        asset.schema = getSchema(instance, asset.type);
        AssetStore.registerAsset(asset);
    }

    public static JsonElement getSchema(Object instance, Type assetType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.withObjectMapper(objectMapper);
        configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);

        Set<String> localPossibleBlockStates = new HashSet<>();
        if (instance instanceof Block block) {
            if (block.isFilamentBlock()) {
                SimpleBlock filamentBlock = block.asFilamentBlock();
                var possibleStates = filamentBlock.getStateDefinition().getPossibleStates();

                Set<Property<?>> varyingProperties = new HashSet<>();
                Map<Property<?>, Set<Comparable<?>>> propertyValues = new HashMap<>();

                for (BlockState state : possibleStates) {
                    var filteredState = filamentBlock.behaviourModifiedBlockState(state, state);

                    for (var entry : filteredState.getValues().toList()) {
                        propertyValues.computeIfAbsent(entry.property(), k -> new HashSet<>()).add(entry.value());
                    }
                }

                for (Map.Entry<Property<?>, Set<Comparable<?>>> entry : propertyValues.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        varyingProperties.add(entry.getKey());
                    }
                }

                Property<?> waterloggedProp = BlockStateProperties.WATERLOGGED;
                for (BlockState state : possibleStates) {
                    var filteredState = filamentBlock.behaviourModifiedBlockState(state, state);

                    StringBuilder n = new StringBuilder();
                    StringBuilder nNoWaterlogged = new StringBuilder();

                    boolean addedAny = false;
                    boolean addedAnyNoWaterlogged = false;
                    boolean hasWaterlogged = false;

                    for (var entry : filteredState.getValues().toList()) {
                        Property<?> prop = entry.property();

                        if (varyingProperties.contains(prop)) {
                            String keyValue = entry.valueName();

                            n.append(keyValue).append(",");
                            addedAny = true;

                            if (prop.equals(waterloggedProp) || prop.getName().equals("waterlogged")) {
                                hasWaterlogged = true;
                            } else {
                                nNoWaterlogged.append(keyValue).append(",");
                                addedAnyNoWaterlogged = true;
                            }
                        }
                    }

                    if (addedAny) n.deleteCharAt(n.length() - 1);
                    if (addedAnyNoWaterlogged) nNoWaterlogged.deleteCharAt(nNoWaterlogged.length() - 1);

                    String str = n.toString();
                    if (!str.isEmpty()) {
                        localPossibleBlockStates.add(str);
                    }

                    if (hasWaterlogged) {
                        String strNoWater = nNoWaterlogged.toString();
                        if (!strNoWater.isEmpty()) localPossibleBlockStates.add(strNoWater);
                    }
                }
            }
        }

        if (localPossibleBlockStates.isEmpty()) {
            localPossibleBlockStates.add("default");
        }

        List<String> localRequiredItems = new ArrayList<>();
        if (instance instanceof FilamentItem item) {
            for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> entry : item.getBehaviours()) {
                if (entry.getValue() instanceof ItemPredicateModelProvider itemBehaviour) {
                    localRequiredItems.addAll(itemBehaviour.requiredModels());
                }
            }
        }

        configBuilder.forTypesInGeneral().withCustomDefinitionProvider((javaType, context) -> {
            Class<?> rawClass = javaType.getErasedType();

            if (STRING_TYPES.contains(rawClass)) return inlineString(context);
            if (rawClass == Vector2f.class) return inlineNumberArray(context, 2, 2);
            if (rawClass == Vector3f.class) return inlineNumberArray(context, 3, 3);
            if (rawClass == Quaternionf.class) return inlineNumberArray(context, 3, 3);

            if (rawClass == BlockResource.class) {
                return new CustomDefinition(context.getGeneratorConfig().createObjectNode().put("type", "object"), CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES) {
                    @Override
                    public ObjectNode getValue() {
                        ObjectNode node = super.getValue();
                        ArrayNode oneOf = node.putArray("oneOf");

                        ObjectNode parentSchema = inlineString(context).getValue();

                        ObjectNode modelsBranch = context.getGeneratorConfig().createObjectNode();
                        ObjectNode mbProps = modelsBranch.putObject("properties");
                        mbProps.set("parent", parentSchema.deepCopy());

                        ObjectNode modelMap = mbProps.putObject("models").put("type", "object");
                        ObjectNode modelValueSchema = context.createDefinitionReference(context.getTypeContext().resolve(PolymerBlockModel.class));
                        ObjectNode props = modelMap.putObject("properties");
                        for (String state : localPossibleBlockStates) {
                            props.set(state, modelValueSchema.deepCopy());
                        }
                        modelMap.put("additionalProperties", false);
                        modelsBranch.putArray("required").add("models");
                        oneOf.add(modelsBranch);

                        // Branch 2: Textures
                        ObjectNode textureBranch = context.getGeneratorConfig().createObjectNode();
                        ObjectNode tbProps = textureBranch.putObject("properties");
                        tbProps.set("parent", parentSchema.deepCopy());

                        ObjectNode textureMap = tbProps.putObject("textures").put("type", "object");
                        ObjectNode textureValueSchema = context.createDefinitionReference(context.getTypeContext().resolve(BlockResource.TextureBlockModel.class));
                        for (String state : localPossibleBlockStates) {
                            textureMap.putObject("properties").set(state, textureValueSchema.deepCopy());
                        }
                        textureMap.put("additionalProperties", false);

                        ArrayNode tbRequired = textureBranch.putArray("required");
                        tbRequired.add("textures");
                        tbRequired.add("parent");
                        oneOf.add(textureBranch);

                        return node;
                    }
                };
            }

            if (rawClass == BlockStateMappedProperty.class) {
                ResolvedType valueType = javaType.getTypeParameters().isEmpty() ? null : javaType.getTypeParameters().getFirst();
                ObjectNode valueSchema = (valueType != null) ? context.createDefinitionReference(valueType) : context.getGeneratorConfig().createObjectNode();

                ObjectNode mapBranch = context.getGeneratorConfig().createObjectNode();
                mapBranch.put("type", "object");

                ObjectNode propertiesNode = mapBranch.putObject("properties");
                for (String state : localPossibleBlockStates) {
                    propertiesNode.set(state, valueSchema.deepCopy());
                }
                mapBranch.put("additionalProperties", false);

                return anyOf(context, valueSchema, mapBranch);
            }

            if (rawClass == BehaviourConfigMap.class) {
                return inlineObject(context, node -> {
                    ObjectNode props = node.putObject("properties");
                    for (BehaviourType<? extends Behaviour<?>, ?> type : BehaviourRegistry.getTypes()) {
                        ResolvedType res = context.getTypeContext().resolve(type.type());
                        ResolvedType behaviourSuper = res.findSupertype(Behaviour.class);

                        if (behaviourSuper != null && !behaviourSuper.getTypeParameters().isEmpty()) {
                            ResolvedType configType = behaviourSuper.getTypeParameters().getFirst();
                            props.set(type.id().toString(), context.createDefinitionReference(configType));
                        } else {
                            props.putObject(type.id().toString()).put("type", "object");
                        }
                    }

                    node.put("additionalProperties", false);
                });
            }

            if (rawClass == DataComponentMap.class) {
                return inlineObject(context, node -> {
                    ObjectNode props = node.putObject("properties");
                    ObjectMapper mapper = new ObjectMapper();

                    for (DataComponentType<?> compType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
                        var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(compType);
                        if (id == null) continue;

                        Type type = PojoComponents.REGISTERED_COMPONENTS.get(id.toString());

                        if (type != null) {
                            ResolvedType resolved = context.getTypeContext().resolve(type);
                            ObjectNode propertyNode = props.putObject(id.toString());

                            if (resolved.isInstanceOf(List.class)) {
                                ResolvedType itemType = resolved.typeParametersFor(List.class).getFirst();

                                propertyNode.put("type", "array");
                                propertyNode.set("default", mapper.createArrayNode());

                                ObjectNode itemsRef = context.createDefinitionReference(itemType);

                                try {
                                    Object itemDefault = getDefaultInstance(itemType.getErasedType());
                                    if (itemDefault != null) {
                                        itemsRef.set("default", mapper.valueToTree(itemDefault));
                                    }
                                } catch (Exception ignored) {}

                                propertyNode.set("items", itemsRef);

                            } else {
                                ObjectNode ref = context.createDefinitionReference(resolved);
                                if (ref.isObject()) {
                                    propertyNode.setAll(ref);
                                } else {
                                    propertyNode.set("$ref", ref);
                                }

                                try {
                                    Object defaultInstance = getDefaultInstance(resolved.getErasedType());
                                    if (defaultInstance != null) {
                                        propertyNode.set("default", mapper.valueToTree(defaultInstance));
                                    }
                                } catch (Exception ignored) {}
                            }

                        } else {
                            // Fallback for missing/unregistered components
                            ObjectNode fallback = props.putObject(id.toString());
                            fallback.put("type", "object");
                            fallback.put("additionalProperties", true);
                            fallback.put("description", "Unknown component: " + id);
                        }
                    }

                    node.put("additionalProperties", false);
                });
            }

            if (rawClass == PolymerBlockModel.class) {
                ObjectNode stringBranch = stringSchema(context);
                ObjectNode objectBranch = objectSchema(context, node -> {
                    ObjectNode p = node.putObject("properties");
                    p.putObject("model").put("type", "string");
                    p.putObject("x").put("type", "integer");
                    p.putObject("y").put("type", "integer");
                    p.putObject("uvLock").put("type", "boolean");
                    p.putObject("weight").put("type", "integer");
                    node.putArray("required").add("model");
                    node.put("additionalProperties", false);
                });
                return anyOf(context, stringBranch, objectBranch);
            }

            if (rawClass == BlockResource.TextureBlockModel.class) {
                return inlineObject(context, node -> {
                    ObjectNode p = node.putObject("properties");
                    p.putObject("textures").put("type", "object").putObject("additionalProperties").put("type", "string");
                    p.putObject("x").put("type", "integer");
                    p.putObject("y").put("type", "integer");
                    p.putObject("uvLock").put("type", "boolean");
                    p.putObject("weight").put("type", "integer");
                });
            }

            if (Map.class.isAssignableFrom(rawClass)) {
                return inlineObject(context, node -> {
                    ResolvedType valueType = javaType.getTypeParameters().size() >= 2
                            ? javaType.getTypeParameters().get(1)
                            : context.getTypeContext().resolve(Object.class);

                    node.set("additionalProperties", context.createDefinitionReference(valueType));
                });
            }

            if (LOWERCASE_ENUM_TYPES.contains(rawClass)) return inlineLowercaseEnum(context, rawClass);
            return null;
        });

        configBuilder.forFields().withInstanceAttributeOverride((node, fieldScope, context) -> {
            String fieldName = fieldScope.getDeclaredName();
            Class<?> type = fieldScope.getType().getErasedType();

            if ("vanillaItem".equals(fieldName) || type == Item.class) {
                addRegistryEnum(node, BuiltInRegistries.ITEM);
            } else if (type == Block.class) {
                addRegistryEnum(node, BuiltInRegistries.BLOCK);
            } else if (type == BlockState.class) {
                //ArrayNode arr = node.putArray("enum");
                //for (Block block : BuiltInRegistries.BLOCK) {
                //    for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                //        arr.add(serializeBlockState(state));
                //    }
                //}
                node.put("type", "string");
            }

            if (fieldName.equals("models") && !localRequiredItems.isEmpty()) {
                ObjectNode props = node.putObject("properties");
                for (String modelId : localRequiredItems) {
                    props.putObject(modelId).put("type", "string");
                }
            }
        });

        configBuilder.forFields().withPropertyNameOverrideResolver(fieldScope -> toSnakeCase(fieldScope.getDeclaredName()));
        configBuilder.forFields().withDescriptionResolver(field -> {
            Description ann = field.getAnnotationConsideringFieldAndGetter(Description.class);
            return ann != null ? ann.value() : null;
        });

        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);

        return JsonParser.parseString(generator.generateSchema(assetType).toString());
    }

    private static Object getDefaultInstance(Class<?> clazz) throws Exception {
        if (List.class.isAssignableFrom(clazz)) return new ArrayList<>();
        if (Map.class.isAssignableFrom(clazz)) return new HashMap<>();
        if (Set.class.isAssignableFrom(clazz)) return new HashSet<>();

        if (clazz == String.class) return "";
        if (clazz == Integer.class || clazz == int.class) return 0;
        if (clazz == Boolean.class || clazz == boolean.class) return false;

        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            if (clazz.isEnum()) {
                Object[] constants = clazz.getEnumConstants();
                return constants.length > 0 ? constants[0] : null;
            }
            return null;
        }
    }

    private static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) result.append('_');
                result.append(Character.toLowerCase(c));
            } else result.append(c);
        }
        return result.toString();
    }

    private static void addRegistryEnum(ObjectNode node, Registry<?> reg) {
        ArrayNode arr = node.putArray("enum");
        reg.keySet().forEach(id -> arr.add(id.toString()));
        node.put("type", "string");
    }

    private static final Set<Class<?>> STRING_TYPES = Set.of(Identifier.class, BlockState.class, Block.class, Item.class, ItemStack.class, Component.class);
    private static final Set<Class<?>> LOWERCASE_ENUM_TYPES = Set.of(Display.BillboardConstraints.class, EquipmentSlot.class, BlockModelType.class, Difficulty.class, MobCategory.class, ItemDisplayContext.class, PushReaction.class, WeatheringCopper.WeatherState.class);

    private static CustomDefinition inlineString(SchemaGenerationContext context) {
        return new CustomDefinition(context.getGeneratorConfig().createObjectNode().put("type", "string"), CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineNumberArray(SchemaGenerationContext context, int min, Integer max) {
        ObjectNode node = context.getGeneratorConfig().createObjectNode().put("type", "array").put("minItems", min);
        if (max != null) node.put("maxItems", max);
        node.putObject("items").put("type", "number");
        return new CustomDefinition(node, CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineLowercaseEnum(SchemaGenerationContext context, Class<?> cls) {
        ObjectNode node = context.getGeneratorConfig().createObjectNode().put("type", "string");
        ArrayNode v = node.putArray("enum");
        for (Object c : cls.getEnumConstants()) v.add(((Enum<?>) c).name().toLowerCase());
        return new CustomDefinition(node, CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineObject(SchemaGenerationContext context, Consumer<ObjectNode> c) {
        ObjectNode n = context.getGeneratorConfig().createObjectNode().put("type", "object");
        c.accept(n);
        return new CustomDefinition(n, CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition anyOf(SchemaGenerationContext context, ObjectNode... s) {
        ObjectNode n = context.getGeneratorConfig().createObjectNode();
        ArrayNode a = n.putArray("anyOf");
        for (ObjectNode x : s) a.add(x);
        return new CustomDefinition(n, CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static ObjectNode stringSchema(SchemaGenerationContext context) {
        return context.getGeneratorConfig().createObjectNode().put("type", "string");
    }

    private static ObjectNode objectSchema(SchemaGenerationContext context, Consumer<ObjectNode> c) {
        ObjectNode n = context.getGeneratorConfig().createObjectNode().put("type", "object");
        c.accept(n);
        return n;
    }
}