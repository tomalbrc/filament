package de.tomalbrc.filament.util.mixin;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface ItemPredicateCustomCheck {
    void setCustomCheck(Predicate<ItemStack> itemStackPredicate);
}
