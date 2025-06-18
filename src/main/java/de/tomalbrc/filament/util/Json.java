package de.tomalbrc.filament.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.bil.json.SimpleCodecDeserializer;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.behaviour.BehaviourList;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Json {
    private static final Set<String> BEHAVIOUR_ALIASES = ImmutableSet.of("behavior", "behaviors", "behaviours");

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeHierarchyAdapter(BlockState.class, new BlockStateDeserializer())
            .registerTypeHierarchyAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeHierarchyAdapter(Vector2f.class, new Vector2fDeserializer())
            .registerTypeHierarchyAdapter(Quaternionf.class, new QuaternionfDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new SimpleCodecDeserializer<>(ResourceLocation.CODEC))
            .registerTypeHierarchyAdapter(Component.class, new ComponentDeserializer())
            .registerTypeHierarchyAdapter(DataComponentMap.class, new DataComponentsDeserializer())
            .registerTypeHierarchyAdapter(EquipmentSlot.class, new LowercaseEnumDeserializer<>(EquipmentSlot.class))
            .registerTypeHierarchyAdapter(BlockModelType.class, new LowercaseEnumDeserializer<>(BlockModelType.class))
            .registerTypeHierarchyAdapter(Difficulty.class, new LowercaseEnumDeserializer<>(Difficulty.class))
            .registerTypeHierarchyAdapter(MobCategory.class, new LowercaseEnumDeserializer<>(MobCategory.class))
            .registerTypeHierarchyAdapter(ItemDisplayContext.class, new LowercaseEnumDeserializer<>(ItemDisplayContext.class))
            .registerTypeHierarchyAdapter(PushReaction.class, new LowercaseEnumDeserializer<>(PushReaction.class))
            .registerTypeHierarchyAdapter(WeatheringCopper.WeatherState.class, new LowercaseEnumDeserializer<>(WeatheringCopper.WeatherState.class))
            .registerTypeHierarchyAdapter(Block.class, new RegistryDeserializer<>(BuiltInRegistries.BLOCK))
            .registerTypeHierarchyAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .registerTypeHierarchyAdapter(SoundEvent.class, new RegistryDeserializer<>(BuiltInRegistries.SOUND_EVENT))
            .registerTypeHierarchyAdapter(BehaviourConfigMap.class, new BehaviourConfigMap.Deserializer())
            .registerTypeHierarchyAdapter(BehaviourList.class, new BehaviourList.Deserializer())
            .registerTypeHierarchyAdapter(BlockStateMappedProperty.class, new BlockStateMappedPropertyDeserializer<>())
            .registerTypeHierarchyAdapter(PolymerBlockModel.class, new PolymerBlockModelDeserializer())
            .create();

    public static List<InputStream> yamlToJson(InputStream inputStream) {
        Yaml yaml = new Yaml();
        var documents = yaml.loadAll(inputStream);
        List<InputStream> list = new ObjectArrayList<>();
        for (Object document : documents) {
            if (document instanceof Map) {
                @SuppressWarnings("unchecked")
                var documentMap = (Map<String, Object>) document;
                document = Json.camelToSnakeCase(documentMap);
            }
            String jsonString = Json.GSON.toJson(document);
            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            list.add(stream);
        }
        return list;
    }


    public static InputStream camelToSnakeCase(InputStream inputStream) {
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
        } else {
            stream = new ByteArrayInputStream(gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        return stream;
    }

    public static Map<String, Object> camelToSnakeCase(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = camelToSnakeCaseKey(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map1) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) map1;
                value = camelToSnakeCase(nestedMap);
            }
            result.put(key, value);
        }
        return result;
    }

    @Nullable
    private static String aliasReplace(String string) {
        if (BEHAVIOUR_ALIASES.contains(string)) {
            return "behaviour";
        }
        return null;
    }

    @NotNull
    private static String camelToSnakeCaseKey(String key) {
        String replacement = aliasReplace(key);
        if (replacement != null) {
            return replacement;
        }

        StringBuilder snakeCase = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                snakeCase.append('_');
            }
            snakeCase.append(Character.toLowerCase(c));
        }
        return snakeCase.toString();
    }

    public static class BlockStateMappedPropertyDeserializer<T> implements JsonDeserializer<BlockStateMappedProperty<T>> {
        @Override
        public BlockStateMappedProperty<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                Type propertyType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
                T t = context.deserialize(primitive, propertyType);
                return new BlockStateMappedProperty<>(t);
            } else if (json.isJsonObject()) {
                ParameterizedType mapType = getParameterizedType((ParameterizedType) typeOfT);
                Map<String, T> map = context.deserialize(json, mapType);
                return new BlockStateMappedProperty<>(map);
            }

            throw new JsonParseException("Invalid format for MappedProperty");
        }

        @NotNull
        private static ParameterizedType getParameterizedType(ParameterizedType typeOfT) {
            Type propertyType = typeOfT.getActualTypeArguments()[0];
            return new ParameterizedType() {
                @Override
                public Type @NotNull [] getActualTypeArguments() {
                    return new Type[]{String.class, propertyType};
                }

                @Override
                @NotNull
                public Type getRawType() {
                    return Map.class;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            };
        }
    }

    public static class BlockStateDeserializer implements JsonDeserializer<BlockState> {
        @Override
        public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsString().toLowerCase();

            BlockStateParser.BlockResult parsed;
            try {
                parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, name, false);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid BlockState value: " + name);
            }

            return parsed.blockState();
        }
    }

    public static class LowercaseEnumDeserializer<T extends Enum<T>> implements JsonDeserializer<T> {

        private final Class<T> enumClass;

        public LowercaseEnumDeserializer(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String value = json.getAsString().toLowerCase();
            for (T constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }

            throw new JsonParseException("Invalid " + enumClass.getSimpleName() + " value: " + value);
        }
    }

    public static class PolymerBlockModelDeserializer implements JsonDeserializer<PolymerBlockModel> {
        @Override
        public PolymerBlockModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return PolymerBlockModel.of(ResourceLocation.tryParse(primitive.getAsString()));
                }
            } else if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                ResourceLocation model = ResourceLocation.parse(object.get("model").getAsString());
                int x = object.has("x") ? object.get("x").getAsInt() : 0;
                int y = object.has("y") ? object.get("y").getAsInt() : 0;
                boolean uvLock = object.has("uvLock") && object.get("uvLock").getAsBoolean();
                int weight = object.has("weight") ? object.get("weight").getAsInt() : 1;
                return PolymerBlockModel.of(model, x, y, uvLock, weight);
            }

            throw new JsonParseException("Invalid PolymerBlockModel value: " + json);
        }
    }

    public static class QuaternionfDeserializer implements JsonDeserializer<Quaternionf> {
        @Override
        public Quaternionf deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() < 3) {
                throw new JsonParseException("Array size should be at least 3 for euler angle deserialization.");
            }

            float x = jsonArray.get(0).getAsFloat();
            float y = jsonArray.get(1).getAsFloat();
            float z = jsonArray.get(2).getAsFloat();

            return new Quaternionf().rotateXYZ(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, z * Mth.DEG_TO_RAD);
        }
    }

    public static class Vector3fDeserializer implements JsonDeserializer<Vector3f> {
        @Override
        public Vector3f deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() < 3) {
                throw new JsonParseException("Array size should be at least 3 for Vector3f deserialization.");
            }

            float x = jsonArray.get(0).getAsFloat();
            float y = jsonArray.get(1).getAsFloat();
            float z = jsonArray.get(2).getAsFloat();

            return new Vector3f(x, y, z);
        }
    }

    public static class Vector2fDeserializer implements JsonDeserializer<Vector2f> {
        @Override
        public Vector2f deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() < 2) {
                throw new JsonParseException("Array size should be at least 2 for Vector2f deserialization.");
            }

            float x = jsonArray.get(0).getAsFloat();
            float y = jsonArray.get(1).getAsFloat();

            return new Vector2f(x, y);
        }
    }

    private record RegistryDeserializer<T>(Registry<T> registry) implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.registry.getValue(ResourceLocation.parse(element.getAsString()));
        }
    }

    private record ComponentDeserializer() implements JsonDeserializer<Component> {
        @Override
        public Component deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            return TagParser.QUICK_TEXT.parseText(element.getAsString(), ParserContext.of());
        }
    }

    public static class DataComponentsDeserializer implements JsonDeserializer<DataComponentMap> {
        @Override
        public DataComponentMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            RegistryOps.RegistryInfoLookup registryInfoLookup = createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            DataResult<Pair<DataComponentMap, JsonElement>> result = DataComponentMap.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), jsonElement);

            if (result.resultOrPartial().isEmpty()) {
                Filament.LOGGER.error("Skipping broken components; could not load: {}", jsonElement.toString());
                Filament.LOGGER.error("Minecraft error message: {}", result.error().orElseThrow().message());
                return null;
            } else if (result.error().isPresent()) {
                Filament.LOGGER.warn("Could not load some components: {}", jsonElement.toString());
                Filament.LOGGER.warn("Minecraft error message: {}", result.error().orElseThrow().message());
            }

            return result.resultOrPartial().get().getFirst();
        }

        public static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess) {
            final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
            registryAccess.registries().forEach((registryEntry) -> map.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));
            return new RegistryOps.RegistryInfoLookup() {
                @NotNull
                @SuppressWarnings("unchecked")
                public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                    return Optional.ofNullable((RegistryOps.RegistryInfo<T>) map.get(resourceKey));
                }
            };
        }

        public static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
            return new RegistryOps.RegistryInfo<>(registry, registry, registry.registryLifecycle());
        }
    }
}
