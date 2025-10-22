package de.tomalbrc.filament.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

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
        for (int y = 0; y < this.getRowCount(); ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new BackpackContainerSlot(container, x, y, i, j, lockSlot != -1));
            }
        }
    }
}
