package de.tomalbrc.filament.behaviours;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiConsumer;

public class BehaviourConfigMap {
    private final Map<ResourceLocation, Object> behaviourConfigMap = new Object2ObjectOpenHashMap<>();
    public <T> void put(ResourceLocation resourceLocation, Object config) {
        this.behaviourConfigMap.put(resourceLocation, config);
    }

    public <T> T get(ResourceLocation resourceLocation) {
        return (T) this.behaviourConfigMap.get(resourceLocation);
    }

    public void forEach(BiConsumer<ResourceLocation, Object> biConsumer) {
        this.behaviourConfigMap.forEach(biConsumer);
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
                    resourceLocation = ResourceLocation.fromNamespaceAndPath("filament", entry.getKey());

                Type clazz = BehaviourRegistry.getConfigType(resourceLocation);

                if (clazz == null) {
                    Filament.LOGGER.error("Could not load behaviour " + resourceLocation);
                    continue;
                }

                Object deserialized = jsonDeserializationContext.deserialize(entry.getValue(), clazz);
                behaviourConfigMap.put(resourceLocation, deserialized);
            }
            return behaviourConfigMap;
        }
    }
}
