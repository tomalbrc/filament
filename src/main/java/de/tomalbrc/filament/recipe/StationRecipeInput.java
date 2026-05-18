package de.tomalbrc.filament.recipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import java.util.Set;

public record StationRecipeInput(SimpleContainer container, Set<Integer> fuelSlots, ItemStack virtualFuel) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (fuelSlots.contains(index)) {
            if (!virtualFuel.isEmpty()) {
                return virtualFuel;
            }
        }
        return container.getItem(index);
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }
}