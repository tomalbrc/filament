package de.tomalbrc.filament.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.behaviour.BehaviourConfigMap;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
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
    public static final Set<String> BEHAVIOUR_ALIASES = ImmutableSet.of("behavior", "behaviors", "behaviours");

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeHierarchyAdapter(BlockState.class, new BlockStateDeserializer())
            .registerTypeHierarchyAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
            .registerTypeHierarchyAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeHierarchyAdapter(Vector2f.class, new Vector2fDeserializer())
            .registerTypeHierarchyAdapter(Quaternionf.class, new QuaternionfDeserializer())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeHierarchyAdapter(BlockModelType.class, new BlockModelTypeDeserializer())
            .registerTypeHierarchyAdapter(ItemDisplayContext.class, new ItemDisplayContextDeserializer())
            .registerTypeHierarchyAdapter(DataComponentMap.class, new DataComponentsDeserializer())
            .registerTypeHierarchyAdapter(PushReaction.class, new PushReactionDeserializer())
            .registerTypeHierarchyAdapter(WeatheringCopper.WeatherState.class, new WeatherStateDeserializer())
            .registerTypeHierarchyAdapter(Block.class, new RegistryDeserializer<>(BuiltInRegistries.BLOCK))
            .registerTypeHierarchyAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .registerTypeHierarchyAdapter(SoundEvent.class, new RegistryDeserializer<>(BuiltInRegistries.SOUND_EVENT))
            .registerTypeHierarchyAdapter(BehaviourConfigMap.class, new BehaviourConfigMap.Deserializer())
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
                var d = (Map<String, Object>) document;
                document = Json.camelToSnakeCase(d);
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
        }
        else {
            stream = new ByteArrayInputStream(gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        return stream;
    }

    public static Map<String, Object> camelToSnakeCase(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = camelToSnake(entry.getKey());
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
    private static String camelToSnake(String key) {
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
                parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), name, false);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid BlockState value: " + name);
            }

            return parsed.blockState();
        }
    }

    public static class EquipmentSlotDeserializer implements JsonDeserializer<EquipmentSlot> {
        @Override
        public EquipmentSlot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsString().toLowerCase();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.name().equalsIgnoreCase(name)) {
                    return slot;
                }
            }

            throw new JsonParseException("Invalid EquipmentSlot value: " + name);
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

    private static class BlockModelTypeDeserializer implements JsonDeserializer<BlockModelType> {
        @Override
        public BlockModelType deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Expected string, got " + element);
            }

            String value = element.getAsString().toUpperCase();
            try {
                return BlockModelType.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid BlockModelType value: " + value, e);
            }
        }
    }

    private static class ItemDisplayContextDeserializer implements JsonDeserializer<ItemDisplayContext> {
        @Override
        public ItemDisplayContext deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Expected string, got " + element);
            }

            String value = element.getAsString().toUpperCase();
            try {
                return ItemDisplayContext.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid ItemDisplayContext value: " + value, e);
            }
        }
    }

    private static class PushReactionDeserializer implements JsonDeserializer<PushReaction> {
        @Override
        public PushReaction deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Expected string, got " + element);
            }

            String value = element.getAsString().toUpperCase();
            try {
                return PushReaction.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid PushReaction value: " + value, e);
            }
        }
    }

    private static class WeatherStateDeserializer implements JsonDeserializer<WeatheringCopper.WeatherState> {
        @Override
        public WeatheringCopper.WeatherState deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Expected string, got " + element);
            }

            String value = element.getAsString().toUpperCase();
            try {
                return WeatheringCopper.WeatherState.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid WeatherState value: " + value, e);
            }
        }
    }

    private record RegistryDeserializer<T>(Registry<T> registry) implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.registry.get(ResourceLocation.parse(element.getAsString()));
        }
    }

    public static class DataComponentsDeserializer implements JsonDeserializer<DataComponentMap> {
        @Override
        public DataComponentMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            RegistryOps.RegistryInfoLookup registryInfoLookup = createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            DataResult<Pair<DataComponentMap, JsonElement>> result = DataComponentMap.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), jsonElement);

            if (result.resultOrPartial().isEmpty()) {
                return null;
            }

            return result.resultOrPartial().get().getFirst();
        }

        private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess) {
            final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
            registryAccess.registries().forEach((registryEntry) -> map.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));
            return new RegistryOps.RegistryInfoLookup() {
                public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                    return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(resourceKey));
                }
            };
        }

        private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
            return new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
        }
    }
}
