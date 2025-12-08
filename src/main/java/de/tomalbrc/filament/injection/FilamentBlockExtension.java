package de.tomalbrc.filament.injection;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.block.SimpleBlock;

@SuppressWarnings("unused")
public interface FilamentBlockExtension extends BehaviourHolder {
    default boolean isFilamentBlock() {
        return asFilamentBlock() != null;
    }

    default SimpleBlock asFilamentBlock() {
        return null;
    }

    default BehaviourMap getBehaviours() {
        return null;
    }
}
