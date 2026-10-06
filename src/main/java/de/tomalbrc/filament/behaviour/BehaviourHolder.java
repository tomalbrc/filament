package de.tomalbrc.filament.behaviour;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BehaviourHolder {
    @Nullable
    BehaviourMap getBehaviours();

    @Nullable
    default <T extends Behaviour<E>, E> T get(BehaviourType<T, E> behaviourType) {
        return this.getBehaviours() != null ? this.getBehaviours().get(behaviourType) : null;
    }

    @NotNull
    default <T extends Behaviour<E>, E> T getOrThrow(BehaviourType<T, E> behaviourType) {
        var behaviours = this.getBehaviours();
        if (behaviours == null)
            throw new IllegalStateException();

        var res = behaviours.get(behaviourType);

        if (res == null)
            throw new IllegalStateException();

        return res;
    }

    default <T extends Behaviour<E>, E> boolean has(BehaviourType<T, E> behaviourType) {
        return this.getBehaviours() != null && this.getBehaviours().has(behaviourType);
    }

    default void initBehaviours(BehaviourConfigMap behaviourConfigMap) {
        var behaviours = this.getBehaviours();
        if (behaviours != null)
            behaviours.from(behaviourConfigMap);
    }
}