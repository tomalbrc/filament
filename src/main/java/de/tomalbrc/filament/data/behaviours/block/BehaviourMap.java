package de.tomalbrc.filament.data.behaviours.block;

import com.google.gson.*;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.behaviour.Behaviour;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class BehaviourMap {
    private final Map<ResourceLocation, Behaviour> behaviourMap = new Object2ObjectOpenHashMap<>();
    public void put(ResourceLocation resourceLocation, Behaviour behaviour) {
        behaviourMap.put(resourceLocation, behaviour);
    }

    public <T> T get(ResourceLocation resourceLocation) {
        return (T) behaviourMap.get(resourceLocation);
    }

    public static class Deserializer implements JsonDeserializer<BehaviourMap> {
        @Override
        public BehaviourMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            BehaviourMap behaviourMap = new BehaviourMap();
            for (Map.Entry<String, JsonElement> map : object.entrySet()) {
                ResourceLocation resourceLocation;
                if (map.getKey().contains(":"))
                    resourceLocation = ResourceLocation.parse(map.getKey());
                else
                    resourceLocation = ResourceLocation.fromNamespaceAndPath("filament", map.getKey());

                Type clazz = BehaviourRegistry.get(resourceLocation);
                Object deserialized = jsonDeserializationContext.deserialize(map.getValue(), clazz);
                behaviourMap.put(resourceLocation, (Behaviour) deserialized);
            }
            return behaviourMap;
        }
    }
}
