package de.tomalbrc.filament.util;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

public class FilamentContainer extends SimpleContainer {
    private boolean valid = true;

    private final boolean purge;

    private int watcher = 0;

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

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public void startOpen(Player player) {
        super.startOpen(player);
        if (this.watcher == 0 && this.openCallback != null) {
            this.openCallback.run();
        }
        this.watcher++;
    }

    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);
        this.watcher--;
        if (this.purge && this.watcher == 0)
            this.clearContent();

        if (this.watcher == 0 && this.closeCallback != null) {
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
