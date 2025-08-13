package de.tomalbrc.filament.behaviour;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BehaviourConfigMap {
    private final Map<BehaviourType<? extends Behaviour<?>,?>, Object> behaviourConfigMap = new Object2ObjectOpenHashMap<>();

    public void put(BehaviourType<?,?> type, Object config) {
        this.behaviourConfigMap.put(type, config);
    }

    @SuppressWarnings("unchecked")
    public <T extends Behaviour<E>,E> E get(BehaviourType<T,E> type) {
        return (E) this.behaviourConfigMap.get(type);
    }

    public <T extends Behaviour<E>,E> boolean has(BehaviourType<T,E> type) {
        return this.behaviourConfigMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Behaviour<E>,E> void forEach(BiConsumer<BehaviourType<T,E>, Object> biConsumer) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Object> entry : this.behaviourConfigMap.entrySet()) {
            biConsumer.accept((BehaviourType<T, E>) entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Behaviour<E>,E> boolean test(Predicate<BehaviourType<T,E>> biConsumer) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Object> entry : this.behaviourConfigMap.entrySet()) {
            if (biConsumer.test((BehaviourType<T, E>) entry.getKey()))
                return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends Behaviour<E>,E> Optional<E> getConfig(Predicate<BehaviourType<T,E>> biConsumer) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Object> entry : this.behaviourConfigMap.entrySet()) {
            if (biConsumer.test((BehaviourType<T, E>) entry.getKey()))
                return Optional.ofNullable((E) entry.getValue());
        }

        return Optional.empty();
    }

    public boolean isEmpty() {
        return this.behaviourConfigMap.isEmpty();
    }

    public static class Deserializer implements JsonDeserializer<BehaviourConfigMap> {
        @Override
        public BehaviourConfigMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            BehaviourConfigMap behaviourConfigMap = new BehaviourConfigMap();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                ResourceLocation resourceLocation;
                if (entry.getKey().contains(":"))
                    resourceLocation = ResourceLocation.parse(entry.getKey());
                else
                    resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, entry.getKey());

                var behaviourType = BehaviourRegistry.getType(resourceLocation);

                if (behaviourType == null || behaviourType.configType() == null) {
                    Filament.LOGGER.error("Could not load behaviour " + resourceLocation);
                    continue;
                }
                var clazz = behaviourType.configType();
                Object deserialized = jsonDeserializationContext.deserialize(entry.getValue(), clazz);
                behaviourConfigMap.put(BehaviourRegistry.getType(resourceLocation), deserialized);
            }
            return behaviourConfigMap;
        }
    }
}
