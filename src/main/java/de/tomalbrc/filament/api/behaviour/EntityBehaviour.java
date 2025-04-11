package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.entity.FilamentMob;
import net.minecraft.world.entity.Entity;

public interface EntityBehaviour<T> extends Behaviour<T> {
    default void init(FilamentMob mob, BehaviourHolder behaviourHolder) {
    }

    default void onHurtTarget() {

    }

    default Boolean canCollideWith(Entity entity) {
        return true;
    }

    default void registerGoals(FilamentMob mob) {

    }
}
