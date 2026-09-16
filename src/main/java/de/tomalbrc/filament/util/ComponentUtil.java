package de.tomalbrc.filament.util;

import com.mojang.serialization.JsonOps;
import de.tomalbrc.filament.data.Data;
import de.tomalbrc.filament.injection.DataComponentCopying;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;

public class ComponentUtil {
    public static <T> void addInjectedComponent(Item item, DataComponentType<T> type, T value) {
        ((DataComponentCopying) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).filament$register(new DataComponentCopying.InjectionEntry<>(item.builtInRegistryHolder().key(), type, value));
    }

    public static void addCopyComponentsEntry(Data<?> data, Item item) {
        ((DataComponentCopying)BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).filament$register(new DataComponentCopying.CopyingEntry(item.builtInRegistryHolder().key(), data.vanillaItem().builtInRegistryHolder().key(), (vanillaInitializer, target, provider)-> {
            if (vanillaInitializer != null && data.properties().copyComponents == Boolean.TRUE) {
                var tempBuilder = DataComponentMap.builder();
                vanillaInitializer.run(tempBuilder, provider);
                var built = tempBuilder.build();
                for (TypedDataComponent component : built) {
                    if (!target.contains(component.type()) || DataComponents.COMMON_ITEM_COMPONENTS.has(component.type())) {
                        target.set(component.type(), component.value());
                    }
                }
            }

            var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
            for (var entry : data.getAdditionalComponents().entrySet()) {
                DataComponentType<?> type = entry.getKey();
                var value = entry.getValue();
                var codec = type.codec();
                if (codec != null) target.set((DataComponentType) type, codec.decode(ops, value).getPartialOrThrow().getFirst());
            }
        }));
    }
}
