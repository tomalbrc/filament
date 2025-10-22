package de.tomalbrc.filament.gui;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VirtualChestMenu extends VirtualScreenHandler {
    private final SlotGuiInterface gui;
    private final Container container;

    int lockSlot;

    public VirtualChestMenu(MenuType<?> type, int syncId, SlotGuiInterface gui, Player player, Container container, int lockSlot) {
        super(type, syncId, gui, player);
        this.gui = gui;
        this.container = container;
        this.lockSlot = lockSlot;
        setupSlots(player);

        gui.beforeOpen();
        gui.onOpen();
        if (gui instanceof PaginatedContainerGui paginatedContainerGui) {
            paginatedContainerGui.setScreenHandler(this);
        }

        container.startOpen(player);
    }

    @Override
    public void broadcastFullState() {

    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return this.gui.quickMove(i);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    protected void addInventoryHotbarSlots(Container container, int x, int y) {
        if (lockSlot == -1) {
            super.addInventoryHotbarSlots(container, x, y);
        } else {
            for (int slot = 0; slot < 9; ++slot) {
                this.addSlot(new BackpackHotbarSlot(container, lockSlot, slot, x + slot * 18, y));
            }
        }
    }

    @Override
    protected void setupSlots(Player player) {
        if (this.gui == null) // fails when called from VirtualScreenHandler ctor
            return;

        int n;
        int m;

        for (n = 0; n < this.gui.getVirtualSize(); ++n) {
            Slot slot = this.gui.getSlotRedirect(n);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(gui, n, 0, 0));
            }
        }

        if (gui.isIncludingPlayer()) {
            int size = this.gui.getHeight() * this.gui.getWidth();
            for (n = 0; n < 4; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new VirtualSlot(gui, m + n * 9 + size, 0, 0));
                }
            }
        } else {
            Inventory playerInventory = player.getInventory();
            for (n = 0; n < 3; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 0, 0));
                }
            }

            addInventoryHotbarSlots(playerInventory, n, 0);
        }
    }
}