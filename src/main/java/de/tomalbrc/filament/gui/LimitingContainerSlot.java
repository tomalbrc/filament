package de.tomalbrc.filament.gui;

import de.tomalbrc.filament.registry.FilamentComponents;
import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class LimitingContainerSlot extends Slot {
    private final boolean fromBackpack;

    public LimitingContainerSlot(Container container, int x, int y, int i, int j, boolean fromBackpack) {
        super(container, x + y * 9, i + x * 18, j + y * 18);
        this.fromBackpack = fromBackpack;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (FilamentContainer.isPickUpContainer(container) || fromBackpack)
            return FilamentContainer.isPickUpContainer(container) ? itemStack.getItem().canFitInsideContainerItems() && !itemStack.has(FilamentComponents.BACKPACK) : super.mayPlace(itemStack);

        return super.mayPlace(itemStack);
    }
}
