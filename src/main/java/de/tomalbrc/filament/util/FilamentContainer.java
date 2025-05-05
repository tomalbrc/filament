package de.tomalbrc.filament.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FilamentContainer extends SimpleContainer {
    List<ServerPlayer> menus = new ObjectArrayList<>();

    private boolean valid = true;

    private final boolean purge;

    private Runnable closeCallback;
    private Runnable openCallback;

    public FilamentContainer(int size, boolean purge) {
        super(size);
        this.purge = purge;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.valid;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return this.valid;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.valid;
    }

    public void setValid(boolean valid) {
        if (!valid) {
            for (ServerPlayer player : this.menus) {
                player.closeContainer();
            }
        }
        this.valid = valid;
    }

    @Override
    public void startOpen(Player player) {
        super.startOpen(player);
        if (player.isSpectator())
            return;

        if (this.menus.isEmpty() && this.openCallback != null) {
            this.openCallback.run();
        }

        if (player instanceof ServerPlayer serverPlayer)
            this.menus.add(serverPlayer);
    }

    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);
        if (player instanceof ServerPlayer)
            this.menus.remove(player);
        if (this.purge && this.menus.isEmpty())
            this.clearContent();

        if (this.menus.isEmpty() && this.closeCallback != null) {
            this.closeCallback.run();
        }
    }

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void setOpenCallback(Runnable openCallback) {
        this.openCallback = openCallback;
    }
}
