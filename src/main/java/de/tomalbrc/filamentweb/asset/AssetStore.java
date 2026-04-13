package de.tomalbrc.filamentweb.asset;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.ItemPredicateModelProvider;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.resource.BlockResource;
import de.tomalbrc.filament.data.resource.ItemResource;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.util.annotation.AssetRef;
import de.tomalbrc.filament.util.annotation.Description;
import de.tomalbrc.filament.util.annotation.RegistryRef;
import de.tomalbrc.filamentweb.EditorServer;
import de.tomalbrc.filamentweb.util.PojoComponents;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
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
import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// All of this schema gen is extremely cursed ngl
public class AssetStore {
    public static byte[] DEFAULT_MODEL;

    static Map<UUID, Asset> assetsByUuid = new ConcurrentHashMap<>();
    static Map<Path, Asset> assetsByPath = new ConcurrentHashMap<>();

    public static void registerAsset(Asset asset) {
        if (asset.path == null || assetsByPath.containsKey(asset.path)) return;

        assetsByUuid.put(asset.uuid, asset);
        assetsByPath.put(asset.path, asset);
    }

    public static Asset getAsset(UUID uuid) {
        return assetsByUuid.get(uuid);
    }

    public static Map<UUID, Asset> getAssetsByUuid() {
        return assetsByUuid;
    }

    public static void registerAssetFromPath(Data<?> data, Class<?> clazz) {
        Asset asset = new Asset();
        asset.uuid = UUID.randomUUID();
        asset.data = data;
        asset.path = data.filepath;
        asset.type = clazz;
        AssetStore.registerAsset(asset);
    }

    public static JsonElement generateSchema(Object instance, Type assetType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.withObjectMapper(objectMapper);
        configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);

        Set<String> localPossibleItemStates = new HashSet<>();
        if (instance instanceof ItemData itemData) {
            itemData.behaviour().forEach((type, config) -> {
                if (ItemPredicateModelProvider.class.isAssignableFrom(type.type())) {
                    var ins = (ItemPredicateModelProvider)type.createInstance(config);
                    localPossibleItemStates.addAll(ins.requiredModels());
                }
            });
        }

        if (localPossibleItemStates.isEmpty()) {
            localPossibleItemStates.add("default");
        }

        Set<String> localPossibleBlockStates = new HashSet<>();
        if (instance instanceof BlockData blockData) {
            localPossibleBlockStates.addAll(getPossibleBlockStates(blockData));
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

        Set<String> requestedRegistries = new HashSet<>();
        Set<AssetRef.Type> requestedAssets = new HashSet<>();

        configBuilder.forTypesInGeneral().withCustomDefinitionProvider((javaType, context) -> {
            Class<?> rawClass = javaType.getErasedType();

            if (STRING_TYPES.contains(rawClass)) return inlineString(context);
            if (rawClass == Vector2f.class) return inlineNumberArray(context, 2, 2);
            if (rawClass == Vector3f.class) return inlineNumberArray(context, 3, 3);
            if (rawClass == Quaternionf.class) return inlineNumberArray(context, 3, 3);

            if (rawClass == BlockResource.class) {
                return getBlockResourceDefinition(context, localPossibleBlockStates, requestedAssets);
            }
            if (rawClass == ItemResource.class) {
                return getItemResourceDefinition(context, localPossibleItemStates, requestedAssets);
            }

            if (rawClass == BlockStateMappedProperty.class) {
                return getBlockStateMappedDefinition(javaType, context, localPossibleBlockStates);
            }

            if (rawClass == BehaviourConfigMap.class) {
                return getBehaviourMapDefinition(context);
            }

            if (rawClass == DataComponentMap.class) {
                return getComponentsDefinition(context);
            }

            if (rawClass == PolymerBlockModel.class) {
                return getPolymerBlockModelDefinition(context, requestedAssets);
            }

            if (Map.class.isAssignableFrom(rawClass)) {
                return inlineObject(context, node -> {
                    node.put("type", "object");

                    ResolvedType valueType = javaType.getTypeParameters().size() >= 2 ? javaType.getTypeParameters().get(1) : context.getTypeContext().resolve(Object.class);

                    if (valueType != null && valueType.getErasedType() != Object.class) {
                        node.set("additionalProperties", context.createDefinitionReference(valueType));
                    } else {
                        node.put("additionalProperties", true);
                    }
                });
            }

            if (StringRepresentable.class.isAssignableFrom(rawClass) && rawClass.isEnum()) {
                return inlineStringRepresentableEnum(context, rawClass);
            }

            if (LOWERCASE_ENUM_TYPES.contains(rawClass)) return inlineLowercaseEnum(context, rawClass);
            return null;
        });

        //configBuilder.forFields().withRequiredCheck(field -> field.getType().getErasedType().isPrimitive() || field.getAnnotationConsideringFieldAndGetter(Nullable.class) == null || field.getAnnotationConsideringFieldAndGetter(org.jspecify.annotations.Nullable.class) == null);

        configBuilder.forFields().withInstanceAttributeOverride((node, fieldScope, context) -> {
            String fieldName = fieldScope.getDeclaredName();
            ResolvedType fieldType = fieldScope.getType();

            boolean isCollection = fieldType.isInstanceOf(Collection.class) || fieldType.isArray();
            boolean isMap = fieldType.isInstanceOf(Map.class);

            RegistryRef registryRef = fieldScope.getAnnotationConsideringFieldAndGetter(RegistryRef.class);
            if (registryRef == null) {
                registryRef = findTypeUseAnnotation(fieldScope, RegistryRef.class);
            }

            AssetRef assetRef = fieldScope.getAnnotationConsideringFieldAndGetter(AssetRef.class);
            if (assetRef == null) {
                assetRef = findTypeUseAnnotation(fieldScope, AssetRef.class);
            }

            if (registryRef != null) {
                String defKey = registryRef.value().replace(":", "_") + (registryRef.withHash() ? "_hash" : "") + (registryRef.tagsOnly() ? "_tags_only" : (registryRef.tags() ? "_tags" : ""));
                requestedRegistries.add(defKey);
                String refPath = "#/$defs/" + defKey;

                node.removeAll();

                if (isCollection) {
                    node.put("type", "array");
                    node.putObject("items").put("$ref", refPath);
                } else if (isMap) {
                    node.put("type", "object");
                    node.putObject("additionalProperties").put("$ref", refPath);
                } else {
                    node.put("$ref", refPath);
                }
            } else if (assetRef != null) {
                AssetRef.Type type = assetRef.value();
                requestedAssets.add(type);

                String refPath = "#/$defs/asset_" + type.name().toLowerCase(Locale.ROOT);

                node.removeAll();

                if (isCollection) {
                    node.put("type", "array");
                    node.putObject("items").put("$ref", refPath);
                } else if (isMap) {
                    node.put("type", "object");
                    node.putObject("additionalProperties").put("$ref", refPath);
                } else {
                    node.put("$ref", refPath);
                }
            }
            else {
                ResolvedType targetType = fieldType;
                if (fieldType.isInstanceOf(Collection.class)) {
                    List<ResolvedType> typeParams = fieldType.typeParametersFor(Collection.class);
                    if (!typeParams.isEmpty()) targetType = typeParams.getFirst();
                } else if (fieldType.isArray()) {
                    targetType = fieldType.getArrayElementType();
                } else if (isMap) {
                    List<ResolvedType> typeParams = fieldType.typeParametersFor(Map.class);
                    if (typeParams.size() >= 2) targetType = typeParams.get(1);
                }

                Class<?> targetClass = targetType != null ? targetType.getErasedType() : Object.class;
                String reqReg = null;

                if (targetClass == Item.class) {
                    reqReg = "item";
                } else if (targetClass == Block.class) {
                    reqReg = "block";
                }

                if (reqReg != null) {
                    requestedRegistries.add(reqReg);
                    String refPath = "#/$defs/" + reqReg;

                    node.removeAll();
                    if (isCollection) {
                        node.put("type", "array");
                        node.putObject("items").put("$ref", refPath);
                    } else if (isMap) {
                        node.put("type", "object");
                        node.putObject("additionalProperties").put("$ref", refPath);
                    } else {
                        node.put("$ref", refPath);
                    }
                } else if (targetClass == BlockState.class) {
                    node.removeAll();
                    if (isCollection) {
                        node.put("type", "array");
                        node.putObject("items").put("type", "string");
                    } else if (isMap) {
                        node.put("type", "object");
                        node.putObject("additionalProperties").put("type", "string");
                    } else {
                        node.put("type", "string");
                    }
                } else if (isMap && !node.has("type")) {
                    // fixme: not sure how to handle this in a nice way
                    node.put("type", "object");
                    if (!node.has("additionalProperties")) {
                        node.put("additionalProperties", true);
                    }
                }
            }

            if (fieldName.equals("models") && !localRequiredItems.isEmpty()) {
                node.removeAll();
                node.put("type", "object");
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

        ObjectNode rootSchema = generator.generateSchema(assetType);

        if (!requestedAssets.isEmpty()) {
            ObjectNode defs = (ObjectNode) rootSchema.get("$defs");
            if (defs == null) {
                defs = rootSchema.putObject("$defs");
            }

            for (AssetRef.Type type : requestedAssets) {
                String key = "asset_" + type.name().toLowerCase(Locale.ROOT);

                if (!defs.has(key)) {
                    ObjectNode assetNode = defs.putObject(key);
                    populateAssetDefinition(assetNode, type);
                }
            }
        }

        if (!requestedRegistries.isEmpty()) {
            ObjectNode defs = (ObjectNode) rootSchema.get("$defs");
            if (defs == null) {
                defs = rootSchema.putObject("$defs");
            }
            for (String req : requestedRegistries) {
                if (!defs.has(req)) {
                    ObjectNode regNode = defs.putObject(req);
                    boolean hash = req.contains("_hash");
                    boolean tagsOnly = req.endsWith("_tags_only");
                    boolean tags = req.endsWith("_tags") || tagsOnly;

                    String regName = req;
                    if (tagsOnly) {
                        regName = req.substring(0, req.length() - 10);
                    } else if (tags) {
                        regName = req.substring(0, req.length() - 5);
                    }

                    populateRegistryDefinition(regNode, regName, tags, tagsOnly, hash);
                }
            }
        }

        return JsonParser.parseString(rootSchema.toString());
    }

    private static <A extends Annotation> A findTypeUseAnnotation(FieldScope fieldScope, Class<A> annotationClass) {
        Field field = fieldScope.getRawMember();
        if (field == null) return null;
        return findTypeUseAnnotationRecursively(field.getAnnotatedType(), annotationClass);
    }

    private static <A extends Annotation> A findTypeUseAnnotationRecursively(AnnotatedType type, Class<A> annotationClass) {
        if (type == null) return null;

        A ann = type.getAnnotation(annotationClass);
        if (ann != null) return ann;

        switch (type) {
            case AnnotatedParameterizedType apt -> {
                AnnotatedType[] args = apt.getAnnotatedActualTypeArguments();
                for (AnnotatedType arg : args) {
                    A found = findTypeUseAnnotationRecursively(arg, annotationClass);
                    if (found != null) return found;
                }
            }
            case AnnotatedArrayType aat -> {
                return findTypeUseAnnotationRecursively(aat.getAnnotatedGenericComponentType(), annotationClass);
            }
            case AnnotatedTypeVariable atv -> {
                for (AnnotatedType bound : atv.getAnnotatedBounds()) {
                    A found = findTypeUseAnnotationRecursively(bound, annotationClass);
                    if (found != null) return found;
                }
            }
            default -> {
            }
        }

        return null;
    }

    private static void populateAssetDefinition(ObjectNode node, AssetRef.Type type) {
        node.put("type", "string");

        ArrayNode arr = node.putArray("enum");

        var assets = EditorServer.RP_ASSETS.get(type);
        if (assets != null) {
            for (String id : assets) {
                arr.add(id);
            }
        }
    }

    private static Set<String> getPossibleBlockStates(BlockData blockData) {
        var localPossibleBlockStates = new HashSet<String>();

        var block = BuiltInRegistries.BLOCK.getValue(blockData.id());
        if (block != null && block.isFilamentBlock()) {
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

                        n.append(prop.getName()).append("=").append(keyValue).append(",");
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

        return localPossibleBlockStates;
    }

    private static @NonNull CustomDefinition getBlockStateMappedDefinition(ResolvedType javaType, SchemaGenerationContext context, Set<String> localPossibleBlockStates) {
        ResolvedType valueType = javaType.getTypeParameters().isEmpty() ? null : javaType.getTypeParameters().getFirst();
        ObjectNode valueSchema = (valueType != null) ? context.createDefinition(valueType) : context.getGeneratorConfig().createObjectNode();
        valueSchema.put("title", "Single value");

        ObjectNode mapBranch = context.getGeneratorConfig().createObjectNode();
        mapBranch.put("title", "Per-state map");
        mapBranch.put("type", "object");

        var required = mapBranch.putArray("required");
        ObjectNode propertiesNode = mapBranch.putObject("properties");
        for (String state : localPossibleBlockStates) {
            var copy = valueSchema.deepCopy();
            copy.put("title", state);

            propertiesNode.set(state, copy);
            required.add(state);
        }

        return anyOf(context, valueSchema, mapBranch);
    }

    private static @NonNull CustomDefinition getBehaviourMapDefinition(SchemaGenerationContext context) {
        return inlineObject(context, node -> {
            ObjectNode props = node.putObject("properties");
            node.remove("required");
            for (BehaviourType<? extends Behaviour<?>, ?> type : BehaviourRegistry.getTypes()) {
                ResolvedType res = context.getTypeContext().resolve(type.type());
                ResolvedType behaviourSuper = res.findSupertype(Behaviour.class);

                if (behaviourSuper != null && !behaviourSuper.getTypeParameters().isEmpty()) {
                    ResolvedType configType = behaviourSuper.getTypeParameters().getFirst();
                    props.set(type.id().toString(), context.createDefinition(configType));
                } else {
                    props.putObject(type.id().toString()).put("type", "object");
                }
            }

            node.put("additionalProperties", false);
        });
    }

    private static @NonNull CustomDefinition getComponentsDefinition(SchemaGenerationContext context) {
        return inlineObject(context, node -> {
            ObjectNode props = node.putObject("properties");
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            for (DataComponentType<?> compType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
                var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(compType);
                if (id == null) continue;
                Type type = PojoComponents.REGISTERED_COMPONENTS.get(id.toString());
                if (type == null) continue;

                ObjectNode propertyNode = props.putObject(id.toString());
                ResolvedType resolved = context.getTypeContext().resolve(type);

                if (resolved.isInstanceOf(List.class)) {
                    ResolvedType itemType = resolved.typeParametersFor(List.class).getFirst();
                    propertyNode.put("type", "array");
                    propertyNode.set("items", context.createDefinitionReference(itemType));
                } else {
                    props.set(id.toString(), context.createDefinitionReference(resolved));
                }

                Object defaultInstance = getDefaultInstance(resolved.getErasedType());
                if (defaultInstance != null && defaultInstance.getClass() != Object.class) {
                    propertyNode.set("default", mapper.valueToTree(defaultInstance));
                }
            }

            node.putArray("required");
            node.put("additionalProperties", false);
        });
    }

    private static @NonNull CustomDefinition getPolymerBlockModelDefinition(SchemaGenerationContext context, Set<AssetRef.Type> requestedAssets) {
        return inlineObject(context, node -> {
            node.put("title", "Block State Model");
            ObjectNode p = node.putObject("properties");
            requestedAssets.add(AssetRef.Type.MODEL);
            String modelRefPath = "#/$defs/asset_" + AssetRef.Type.MODEL.name().toLowerCase(Locale.ROOT);
            p.putObject("model").put("$ref", modelRefPath);

            p.putObject("x").put("type", "integer");
            p.putObject("y").put("type", "integer");
            p.putObject("uvlock").put("type", "boolean");
            p.putObject("weight").put("type", "integer");
            node.putArray("required").add("model");
            node.put("additionalProperties", false);
        });
    }

    private static @NonNull CustomDefinition getBlockResourceDefinition(SchemaGenerationContext context, Set<String> localPossibleBlockStates, Set<AssetRef.Type> requestedAssets) {
        return new CustomDefinition(context.getGeneratorConfig().createObjectNode().put("type", "object"), CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES) {
            @Override
            public ObjectNode getValue() {
                ObjectNode node = super.getValue();
                ArrayNode oneOf = node.putArray("oneOf");

                ObjectNode parentSchema = inlineString(context).getValue();

                ObjectNode modelsBranch = context.getGeneratorConfig().createObjectNode();
                modelsBranch.put("additionalProperties", false);
                modelsBranch.put("title", "Block models");
                ObjectNode mbProps = modelsBranch.putObject("properties");

                ObjectNode modelMap = mbProps.putObject("models").put("type", "object");
                ObjectNode modelExplicitProps = modelMap.putObject("properties");
                ObjectNode modelPatterns = modelMap.putObject("patternProperties");

                ObjectNode modelValueSchema = context.getGeneratorConfig().createObjectNode();
                ArrayNode modelAnyOf = modelValueSchema.putArray("anyOf");

                requestedAssets.add(AssetRef.Type.MODEL);
                String modelRefPath = "#/$defs/asset_" + AssetRef.Type.MODEL.name().toLowerCase(Locale.ROOT);
                ObjectNode modelPathNode = context.getGeneratorConfig().createObjectNode()
                        .put("$ref", modelRefPath)
                        .put("title", "Model Path");
                modelAnyOf.add(modelPathNode);

                modelAnyOf.add(context.createDefinition(context.getTypeContext().resolve(PolymerBlockModel.class)));

                ArrayNode modelRequired = modelMap.putArray("required");
                localPossibleBlockStates.forEach((k) -> {
                    modelExplicitProps.set(k, modelValueSchema.deepCopy());
                    modelRequired.add(k);
                });

                modelPatterns.set("^.*$", modelValueSchema.deepCopy());
                modelMap.put("additionalProperties", false);

                modelsBranch.putArray("required").add("models");
                oneOf.add(modelsBranch);

                ObjectNode textureBranch = context.getGeneratorConfig().createObjectNode();
                textureBranch.put("additionalProperties", false);
                textureBranch.put("title", "Generated with Texture");
                ObjectNode tbProps = textureBranch.putObject("properties");
                tbProps.set("parent", parentSchema.deepCopy());

                ObjectNode textureMap = tbProps.putObject("textures").put("type", "object");
                ObjectNode textureExplicitProps = textureMap.putObject("properties");
                ObjectNode texturePatterns = textureMap.putObject("patternProperties");

                ObjectNode textureValueSchema = context.createDefinition(context.getTypeContext().resolve(BlockResource.TextureBlockModel.class));

                ArrayNode textureRequired = textureMap.putArray("required");
                localPossibleBlockStates.forEach((k) -> {
                    textureExplicitProps.set(k, textureValueSchema.deepCopy());
                    textureRequired.add(k);
                });

                texturePatterns.set("^.*$", textureValueSchema.deepCopy());
                textureMap.put("additionalProperties", false);

                ArrayNode tbRequired = textureBranch.putArray("required");
                tbRequired.add("textures");
                tbRequired.add("parent");
                oneOf.add(textureBranch);

                return node;
            }
        };
    }

    private static @NonNull CustomDefinition getItemResourceDefinition(SchemaGenerationContext context, Set<String> localPossibleItemStates, Set<AssetRef.Type> requestedAssets) {
        return new CustomDefinition(context.getGeneratorConfig().createObjectNode().put("type", "object"), CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES) {
            @Override
            public ObjectNode getValue() {
                ObjectNode node = super.getValue();
                ArrayNode oneOf = node.putArray("oneOf");

                ObjectNode parentSchema = inlineString(context).getValue();

                ObjectNode modelsBranch = context.getGeneratorConfig().createObjectNode();
                modelsBranch.put("additionalProperties", false);
                modelsBranch.put("title", "Item models");
                ObjectNode mbProps = modelsBranch.putObject("properties");

                ObjectNode modelMap = mbProps.putObject("models").put("type", "object");
                ObjectNode modelExplicitProps = modelMap.putObject("properties");
                ObjectNode modelPatterns = modelMap.putObject("patternProperties");

                requestedAssets.add(AssetRef.Type.MODEL);
                String modelRefPath = "#/$defs/asset_" + AssetRef.Type.MODEL.name().toLowerCase(Locale.ROOT);
                ObjectNode modelValueSchema = context.getGeneratorConfig().createObjectNode()
                        .put("$ref", modelRefPath)
                        .put("title", "Model Path");

                ArrayNode modelRequired = modelMap.putArray("required");
                localPossibleItemStates.forEach((k) -> {
                    modelExplicitProps.set(k, modelValueSchema.deepCopy());
                    modelRequired.add(k);
                });

                modelPatterns.set("^.*$", modelValueSchema.deepCopy());
                modelMap.put("additionalProperties", false);

                modelsBranch.putArray("required").add("models");
                oneOf.add(modelsBranch);

                ObjectNode textureBranch = context.getGeneratorConfig().createObjectNode();
                textureBranch.put("additionalProperties", false);
                textureBranch.put("title", "Generated with Texture");
                ObjectNode tbProps = textureBranch.putObject("properties");
                tbProps.set("parent", parentSchema.deepCopy());

                ObjectNode textureMap = tbProps.putObject("textures").put("type", "object");
                ObjectNode textureExplicitProps = textureMap.putObject("properties");
                ObjectNode texturePatterns = textureMap.putObject("patternProperties");

                requestedAssets.add(AssetRef.Type.TEXTURE);
                String textureRefPath = "#/$defs/asset_" + AssetRef.Type.TEXTURE.name().toLowerCase(Locale.ROOT);
                ObjectNode textureValueSchema = context.getGeneratorConfig().createObjectNode().put("$ref", textureRefPath);

                ArrayNode textureRequired = textureMap.putArray("required");
                localPossibleItemStates.forEach((k) -> {
                    textureExplicitProps.set(k, textureValueSchema.deepCopy());
                    textureRequired.add(k);
                });

                texturePatterns.set("^.*$", textureValueSchema.deepCopy());
                textureMap.put("additionalProperties", false);

                ArrayNode tbRequired = textureBranch.putArray("required");
                tbRequired.add("textures");
                tbRequired.add("parent");
                oneOf.add(textureBranch);

                return node;
            }
        };
    }

    private static CustomDefinition inlineStringRepresentableEnum(SchemaGenerationContext context, Class<?> cls) {
        ObjectNode node = context.getGeneratorConfig().createObjectNode().put("type", "string");
        ArrayNode values = node.putArray("enum");

        Object[] constants = cls.getEnumConstants();
        if (constants != null) {
            for (Object constant : constants) {
                if (constant instanceof StringRepresentable representable) {
                    values.add(representable.getSerializedName());
                } else if (constant instanceof Enum<?> e) {
                    values.add(e.name().toLowerCase());
                }
            }
        }

        return new CustomDefinition(node, CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
    }

    private static void populateRegistryDefinition(ObjectNode node, String registryName, boolean includeTags, boolean tagsOnly, boolean hash) {
        var regId = Identifier.tryParse(registryName.toLowerCase(Locale.ROOT));
        if (regId == null)
            return;

        var reg = Filament.SERVER.registryAccess().lookup(ResourceKey.createRegistryKey(regId));
        if (reg.isEmpty())
            return;

        ArrayNode arr = node.putArray("enum");

        if (!tagsOnly) {
            reg.get().keySet().forEach(id -> arr.add(id.toString()));
        }

        if (includeTags || tagsOnly) {
            reg.get().getTags().forEach(obj -> {
                arr.add((hash ? "#" : "") + obj.key().location());
            });
        }

        node.put("type", "string");
    }

    private static Object getDefaultInstance(Class<?> clazz) {
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
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
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

    private static final Set<Class<?>> STRING_TYPES = Set.of(Identifier.class, BlockState.class, Block.class, Item.class, ItemStack.class, Component.class);
    private static final Set<Class<?>> LOWERCASE_ENUM_TYPES = Set.of(Display.BillboardConstraints.class, EquipmentSlot.class, BlockModelType.class, Difficulty.class, MobCategory.class, ItemDisplayContext.class, PushReaction.class, WeatheringCopper.WeatherState.class);

    private static CustomDefinition inlineString(SchemaGenerationContext context) {
        return new CustomDefinition(context.getGeneratorConfig().createObjectNode().put("type", "string"), CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineNumberArray(SchemaGenerationContext context, int min, Integer max) {
        ObjectNode node = context.getGeneratorConfig().createObjectNode().put("type", "array").put("minItems", min);
        if (max != null) node.put("maxItems", max);
        node.putObject("items").put("type", "number");
        return new CustomDefinition(node, CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineLowercaseEnum(SchemaGenerationContext context, Class<?> cls) {
        ObjectNode node = context.getGeneratorConfig().createObjectNode().put("type", "string");
        ArrayNode v = node.putArray("enum");
        for (Object c : cls.getEnumConstants()) v.add(((Enum<?>) c).name().toLowerCase());
        return new CustomDefinition(node, CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition inlineObject(SchemaGenerationContext context, Consumer<ObjectNode> c) {
        ObjectNode n = context.getGeneratorConfig().createObjectNode().put("type", "object");
        c.accept(n);
        return new CustomDefinition(n, CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
    }

    private static CustomDefinition anyOf(SchemaGenerationContext context, ObjectNode... s) {
        ObjectNode n = context.getGeneratorConfig().createObjectNode();
        ArrayNode a = n.putArray("anyOf");
        for (ObjectNode x : s) a.add(x);
        return new CustomDefinition(n, CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
    }
}