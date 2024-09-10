package de.tomalbrc.filament.behaviours;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Iterator;
import java.util.Map;

public class BehaviourMap implements Iterable<Map.Entry<BehaviourRegistry.BehaviourType<?,?>, Behaviour<?>>> {
    private final Map<BehaviourRegistry.BehaviourType<?,?>, Behaviour<?>> behaviourMap = new Object2ObjectOpenHashMap<>();
    public <T extends Behaviour<E>, E> void put(BehaviourRegistry.BehaviourType<T, E> resourceLocation, Behaviour<?> behaviour) {
        this.behaviourMap.put(resourceLocation, behaviour);
    }

    public <T extends Behaviour<E>, E> T get(BehaviourRegistry.BehaviourType<T, E> type) {
        return (T) this.behaviourMap.get(type);
    }

    public <T extends Behaviour<E>, E> boolean has(BehaviourRegistry.BehaviourType<T, E> type) {
        return this.behaviourMap.containsKey(type);
    }

    public void from(BehaviourConfigMap configMap) {
        if (configMap != null) configMap.forEach((resourceLocation, behaviour) -> {
            this.put(resourceLocation, BehaviourRegistry.create(resourceLocation, behaviour));
        });
    }

    public boolean isEmpty() {
        return this.behaviourMap.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<BehaviourRegistry.BehaviourType<?,?>, Behaviour<?>>> iterator() {
        return this.behaviourMap.entrySet().iterator();
    }
}
