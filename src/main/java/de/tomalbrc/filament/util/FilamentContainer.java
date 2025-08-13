package de.tomalbrc.filament.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;

public class FilamentContainer extends SimpleContainer {
    List<LivingEntity> menus = new ObjectArrayList<>();

    private boolean valid = true;

    private final boolean purge;
    private final BlockEntity blockEntity;

    private Runnable closeCallback;
    private Runnable openCallback;

    public FilamentContainer(BlockEntity blockEntity, int size, boolean purge) {
        super(size);

        this.addListener(x -> blockEntity.setChanged());
        this.blockEntity = blockEntity;
        this.purge = purge;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.valid && !blockEntity.isRemoved();
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return this.valid;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.valid && !blockEntity.isRemoved() && stack.getCount() <= getMaxStackSize(slot) - getItem(slot).getCount();
    }

    public int getMaxStackSize(int slot) {
        return getMaxStackSize();
    }

    public void setValid(boolean valid) {
        if (!valid) {
            for (LivingEntity entity : this.menus) {
                if (entity instanceof ServerPlayer player) player.closeContainer();
            }
        }
        this.valid = valid;
    }

    public boolean hasViewers() {
        return !this.menus.isEmpty();
    }

    @Override
    public void startOpen(Player player) {
        super.startOpen(player);

        if (!player.isSpectator() && this.menus.isEmpty() && this.openCallback != null) {
            this.openCallback.run();
        }

        this.menus.add(player);
    }

    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);

        this.menus.remove(player);

        if (this.menus.isEmpty() && this.closeCallback != null) {
            this.closeCallback.run();
        }

        if (this.purge && this.menus.isEmpty())
            this.clearContent();
    }

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void setOpenCallback(Runnable openCallback) {
        this.openCallback = openCallback;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
