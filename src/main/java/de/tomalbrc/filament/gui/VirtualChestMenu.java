package de.tomalbrc.filament.gui;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VirtualChestMenu extends VirtualScreenHandler {
    private final SlotGuiInterface gui;
    private final Container container;

    public VirtualChestMenu(MenuType<?> type, int syncId, SlotGuiInterface gui, Player player, Container container) {
        super(type, syncId, gui, player);
        this.gui = gui;
        this.container = container;

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
}