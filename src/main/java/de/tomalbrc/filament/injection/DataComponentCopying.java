package de.tomalbrc.filament.injection;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface DataComponentCopying {
    @FunctionalInterface
    interface Applier {
        void apply(@Nullable DataComponentInitializers.InitializerEntry<?> vanillaItemInitializer, DataComponentMap.Builder target, HolderLookup.Provider provider);
    }

    record CustomInitializerEntry(ResourceKey<Item> target, ResourceKey<Item> source, Applier customPatcher) {

    }

    void filament$registerToCopy(CustomInitializerEntry data);
}
