package de.tomalbrc.filament.behaviour;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.util.Constants;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class BehaviourList implements Iterable<Behaviour<?>> {
    public static final BehaviourList EMPTY = new BehaviourList();

    private final List<Behaviour<?>> behaviourList = new ObjectArrayList<>();
    public <T extends Behaviour<E>, E> void add(Behaviour<?> behaviour) {
        this.behaviourList.add(behaviour);
    }

    public void from(BehaviourList configMap) {
        if (configMap != null) configMap.forEach(this::add);
    }

    public boolean isEmpty() {
        return this.behaviourList.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Behaviour<?>> iterator() {
        return this.behaviourList.iterator();
    }

    public static class Deserializer implements JsonDeserializer<BehaviourList> {
        @Override
        public BehaviourList deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonArray array = jsonElement.getAsJsonArray();
            BehaviourList behaviourConfigMap = new BehaviourList();
            for (JsonElement entry : array) {
                JsonObject obj = entry.getAsJsonObject();
                String typeId = obj.get("type").getAsString();
                Identifier id;
                if (typeId.contains(":"))
                    id = Identifier.parse(typeId);
                else
                    id = Identifier.fromNamespaceAndPath(Constants.MOD_ID, typeId);

                var behaviourType = BehaviourRegistry.getType(id);

                if (behaviourType == null || behaviourType.configType() == null) {
                    Filament.LOGGER.error("Could not load behaviour {}", id);
                    continue;
                }

                var clazz = behaviourType.configType();
                Object deserialized = jsonDeserializationContext.deserialize(entry, clazz);
                behaviourConfigMap.add(behaviourType.createInstance(deserialized));
            }
            return behaviourConfigMap;
        }
    }
}
