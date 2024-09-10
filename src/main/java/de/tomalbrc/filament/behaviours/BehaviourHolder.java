package de.tomalbrc.filament.behaviours;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import org.jetbrains.annotations.Nullable;

public interface BehaviourHolder {

    BehaviourMap getBehaviours();

    @Nullable
    default <T extends Behaviour<E>, E> T get(BehaviourRegistry.BehaviourType<T, E> behaviourType) {
        return this.getBehaviours().get(behaviourType);
    }

    default <T extends Behaviour<E>, E> boolean has(BehaviourRegistry.BehaviourType<T, E> behaviourType) {
        return this.getBehaviours().has(behaviourType);
    }

    default void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        this.getBehaviours().from(behaviourConfigMap);
    }
}