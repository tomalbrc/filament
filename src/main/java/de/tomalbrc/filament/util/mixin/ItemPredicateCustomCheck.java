package de.tomalbrc.filament.util.mixin;

import net.minecraft.world.item.ItemInstance;

import java.util.function.Predicate;

public interface ItemPredicateCustomCheck {
    void setCustomCheck(Predicate<ItemInstance> itemStackPredicate);
}
