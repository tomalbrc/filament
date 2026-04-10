package de.tomalbrc.filament.util.mixin;

import net.minecraft.resources.ResourceKey;

public interface RegistryUnfreezer {
    void filament$unfreeze();
    void filament$freeze();

    void filament$hackyRemove(Object t, ResourceKey key);
}
