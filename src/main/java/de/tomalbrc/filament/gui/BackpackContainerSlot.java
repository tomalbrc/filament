package de.tomalbrc.filament.gui;

import de.tomalbrc.filament.registry.FilamentComponents;
import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class BackpackContainerSlot extends Slot {
    private final boolean fromBackpack;

    public BackpackContainerSlot(Container container, int x, int y, int i, int j, boolean fromBackpack) {
        super(container, x + y * 9, i + x * 18, j + y * 18);
        this.fromBackpack = fromBackpack;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (FilamentContainer.isPickUpContainer(container) || fromBackpack)
            return itemStack.getItem().canFitInsideContainerItems() && (!FilamentContainer.isPickUpContainer(container) && !itemStack.has(FilamentComponents.BACKPACK));

        return super.mayPlace(itemStack);
    }
}
