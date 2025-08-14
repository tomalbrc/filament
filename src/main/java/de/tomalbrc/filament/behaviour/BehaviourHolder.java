package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BehaviourHolder {

    BehaviourMap getBehaviours();

    @Nullable
    default <T extends Behaviour<E>, E> T get(BehaviourType<T, E> behaviourType) {
        return this.getBehaviours().get(behaviourType);
    }

    @NotNull
    default <T extends Behaviour<E>, E> T getOrThrow(BehaviourType<T, E> behaviourType) {
        var res = this.getBehaviours().get(behaviourType);

        if (res == null)
            throw new IllegalStateException();

        return this.getBehaviours().get(behaviourType);
    }

    default <T extends Behaviour<E>, E> boolean has(BehaviourType<T, E> behaviourType) {
        return this.getBehaviours().has(behaviourType);
    }

    default void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        this.getBehaviours().from(behaviourConfigMap);
    }
}