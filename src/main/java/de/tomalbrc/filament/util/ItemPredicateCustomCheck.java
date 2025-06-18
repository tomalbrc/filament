package de.tomalbrc.filament.util;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface ItemPredicateCustomCheck {
    void setCustomCheck(Predicate<ItemStack> itemStackPredicate);
}
