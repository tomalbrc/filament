package de.tomalbrc.filament.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackMenu {
    public static AbstractContainerMenu create(int id, Inventory inventory, Container container, int selectedSlot) {
        return new ShulkerBoxMenu(id, inventory, container) {
            @Override
            protected void addInventoryHotbarSlots(Container container, int x, int y) {
                for (int slot = 0; slot < 9; ++slot) {
                    this.addSlot(new BackpackHotbarSlot(container, selectedSlot, slot, x + slot * 18, y));
                }
            }

            public static class BackpackHotbarSlot extends Slot {
                final int selectedSlot;

                public BackpackHotbarSlot(Container container, int selectedSlot, int slot, int x, int y) {
                    super(container, slot, x, y);
                    this.selectedSlot = selectedSlot;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return this.selectedSlot != getContainerSlot() && super.mayPickup(player);
                }

                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    return this.selectedSlot != getContainerSlot() && super.mayPlace(itemStack);
                }

                @Override
                public boolean allowModification(Player player) {
                    return this.selectedSlot != getContainerSlot();
                }
            }
        };
    }

}
