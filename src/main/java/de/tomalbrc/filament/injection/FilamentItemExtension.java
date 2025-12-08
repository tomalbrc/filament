package de.tomalbrc.filament.injection;

import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.BehaviourMap;
import de.tomalbrc.filament.item.FilamentItem;

public interface FilamentItemExtension extends BehaviourHolder {
    default boolean isFilamentItem() {
        return asFilamentItem() != null;
    }

    default FilamentItem asFilamentItem() {
        return null;
    }

    default BehaviourMap getBehaviours() {
        return null;
    }
}
