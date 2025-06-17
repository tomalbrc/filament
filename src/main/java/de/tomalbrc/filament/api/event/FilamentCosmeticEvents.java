package de.tomalbrc.filament.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FilamentCosmeticEvents {
    public static final Event<CosmeticEquipmentChange> EQUIPPED = EventFactory.createArrayBacked(CosmeticEquipmentChange.class, (callbacks) -> (entity, itemStack, itemStack2) -> {
        for (CosmeticEquipmentChange callback : callbacks) {
            callback.onChange(entity, itemStack, itemStack2);
        }
    });

    public static final Event<CosmeticEquipmentChange> UNEQUIPPED = EventFactory.createArrayBacked(CosmeticEquipmentChange.class, (callbacks) -> (entity, itemStack, itemStack2) -> {
        for (CosmeticEquipmentChange callback : callbacks) {
            callback.onChange(entity, itemStack, itemStack2);
        }
    });

    @FunctionalInterface
    public interface CosmeticEquipmentChange {
        void onChange(LivingEntity entity, ItemStack itemStack, ItemStack itemStack2);
    }
}
