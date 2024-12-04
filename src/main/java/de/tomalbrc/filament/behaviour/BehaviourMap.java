package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public class BehaviourMap implements Iterable<Map.Entry<BehaviourType<? extends Behaviour<?>,?>, Behaviour<?>>> {
    private final Map<BehaviourType<?,?>, Behaviour<?>> behaviourMap = new Reference2ObjectOpenHashMap<>();
    public <T extends Behaviour<E>, E> void put(BehaviourType<T, E> resourceLocation, Behaviour<?> behaviour) {
        this.behaviourMap.put(resourceLocation, behaviour);
    }

    @SuppressWarnings("unchecked")
    public <T extends Behaviour<E>, E> T get(BehaviourType<T, E> type) {
        return (T) this.behaviourMap.get(type);
    }

    public <T extends Behaviour<E>, E> boolean has(BehaviourType<T, E> type) {
        return this.behaviourMap.containsKey(type);
    }

    public void from(BehaviourConfigMap configMap) {
        if (configMap != null) configMap.forEach((behaviourType, behaviour) ->
                this.put(behaviourType, behaviourType.createInstance(behaviour))
        );
    }

    public boolean isEmpty() {
        return this.behaviourMap.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Map.Entry<BehaviourType<?,?>, Behaviour<?>>> iterator() {
        return this.behaviourMap.entrySet().iterator();
    }
}
