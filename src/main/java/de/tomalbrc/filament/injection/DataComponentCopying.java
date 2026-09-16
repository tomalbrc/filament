package de.tomalbrc.filament.injection;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface DataComponentCopying {
    @FunctionalInterface
    interface Applier {
        void apply(@Nullable DataComponentInitializers.InitializerEntry<?> vanillaItemInitializer, DataComponentMap.Builder target, HolderLookup.Provider provider);
    }

    record CopyingEntry(ResourceKey<Item> target, ResourceKey<Item> source, Applier customPatcher) {

    }

    record InjectionEntry<T>(ResourceKey<Item> target, DataComponentType<T> type, T value) {

    }

    void filament$register(CopyingEntry data);

    <T> void filament$register(InjectionEntry<T> data);
}
