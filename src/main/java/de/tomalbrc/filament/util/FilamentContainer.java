package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FilamentContainer extends SimpleContainer implements RandomizableContainer {
    List<LivingEntity> menus = new ObjectArrayList<>();

    private boolean valid = true;

    private final boolean purge;
    private final DecorationBlockEntity blockEntity;

    private Runnable closeCallback;
    private Runnable openCallback;

    public FilamentContainer(DecorationBlockEntity blockEntity, int size, boolean purge) {
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
        this.unpackLootTable(player);
        this.unpackLootTable(player);
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

    @Override
    public boolean isEmpty() {
        this.unpackLootTable(null);
        return super.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int n) {
        this.unpackLootTable(null);
        return super.getItem(n);
    }

    @Override
    public @NotNull ItemStack removeItem(int n, int n2) {
        this.unpackLootTable(null);
        return super.removeItem(n, n2);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int n) {
        this.unpackLootTable(null);
        return super.removeItemNoUpdate(n);
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        this.unpackLootTable(null);
        super.setItem(n, itemStack);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        this.unpackLootTable(null);
        return this.items;
    }

    public DecorationBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        var containerLike = DecorationData.getFirstContainer(blockEntity);
        if (containerLike != null)
            return containerLike.getLootTable();
        return null;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
        var containerLike = DecorationData.getFirstContainer(blockEntity);
        if (containerLike != null) containerLike.setLootTable(resourceKey);
    }

    @Override
    public long getLootTableSeed() {
        var containerLike = DecorationData.getFirstContainer(blockEntity);
        if (containerLike != null)
            return containerLike.getLootTableSeed();
        return 0;
    }

    @Override
    public void setLootTableSeed(long l) {
        var containerLike = DecorationData.getFirstContainer(blockEntity);
        if (containerLike != null)
            containerLike.setLootTableSeed(l);
    }

    @Override
    public @NotNull BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public @Nullable Level getLevel() {
        return blockEntity.getLevel();
    }

    public static boolean isPickUpContainer(Container container) {
        ContainerLike containerLike;
        return container instanceof FilamentContainer filamentContainer && (containerLike = DecorationData.getFirstContainer(filamentContainer.getBlockEntity())) != null && containerLike.canPickUp();
    }
}
