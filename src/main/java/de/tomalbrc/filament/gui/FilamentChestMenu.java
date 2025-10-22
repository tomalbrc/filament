package de.tomalbrc.filament.gui;

import de.tomalbrc.filament.util.BackpackHotbarSlot;
import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// "almost vanilla chest menu" but with checks to prevent placement of shulkers & friends into portable filament containers
// and hotbar slot-locking
public class FilamentChestMenu extends ChestMenu {
    int lockSlot;

    public FilamentChestMenu(MenuType<?> menuType, int id, Inventory inventory, Container container, int lockSlot) {
        super(menuType, id, inventory, container, container.getContainerSize() / 9);
        this.lockSlot = lockSlot;
    }

    @Override
    protected void addInventoryHotbarSlots(Container container, int x, int y) {
        if (lockSlot == -1) {
            super.addInventoryHotbarSlots(container, x, y);
        } else {
            for(int i = 0; i < 9; ++i) {
                this.addSlot(new BackpackHotbarSlot(container, lockSlot, i, x + i * 18, y));
            }
        }
    }

    @Override
    public void addChestGrid(Container container, int i, int j) {
        boolean doCheck = FilamentContainer.isPickUpContainer(container);
        for (int y = 0; y < this.getRowCount(); ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(container, x + y * 9, i + x * 18, j + y * 18) {
                    @Override
                    public boolean mayPlace(ItemStack itemStack) {
                        if (doCheck) {
                            return itemStack.getItem().canFitInsideContainerItems();
                        }
                        return super.mayPlace(itemStack);
                    }
                });
            }
        }

    }
}
