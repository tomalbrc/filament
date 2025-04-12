package de.tomalbrc.filament.api.event;

import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.block.SimpleBlockItem;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.data.ItemData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.data.EntityData;
import de.tomalbrc.filament.item.SimpleItem;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.EntityType;

public class FilamentRegistrationEvents {
    public static final Event<ItemRegistration> ITEM = EventFactory.createArrayBacked(ItemRegistration.class, (callbacks) -> (data, item) -> {
        for(ItemRegistration callback : callbacks) {
            callback.registered(data, item);
        }
    });

    public static final Event<BlockRegistration> BLOCK = EventFactory.createArrayBacked(BlockRegistration.class, (callbacks) -> (data, item, block) -> {
        for(BlockRegistration callback : callbacks) {
            callback.registered(data, item, block);
        }
    });

    public static final Event<DecorationRegistration> DECORATION = EventFactory.createArrayBacked(DecorationRegistration.class, (callbacks) -> (data, item, block) -> {
        for(DecorationRegistration callback : callbacks) {
            callback.registered(data, item, block);
        }
    });

    public static final Event<EntityRegistration> ENTITY = EventFactory.createArrayBacked(EntityRegistration.class, (callbacks) -> (data, entityType) -> {
        for(EntityRegistration callback : callbacks) {
            callback.registered(data, entityType);
        }
    });


    @FunctionalInterface
    public interface EntityRegistration {
        void registered(EntityData data, EntityType<?> entityType);
    }

    @FunctionalInterface
    public interface ItemRegistration {
        void registered(ItemData itemData, SimpleItem item);
    }

    @FunctionalInterface
    public interface BlockRegistration {
        void registered(BlockData blockData, SimpleBlockItem item, SimpleBlock block);
    }

    @FunctionalInterface
    public interface DecorationRegistration {
        void registered(DecorationData decorationData, DecorationItem item, DecorationBlock decorationBlock);
    }
}
