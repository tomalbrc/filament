package de.tomalbrc.filament.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class BackpackHotbarSlot extends Slot {
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

    @Override
    public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        return super.tryRemove(count, decrement, player);
    }
}
