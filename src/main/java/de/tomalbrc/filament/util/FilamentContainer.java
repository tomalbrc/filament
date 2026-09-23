package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.block.entity.DecorationLike;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FilamentContainer extends SimpleContainer implements RandomizableContainer {
    List<ContainerUser> menus = new ObjectArrayList<>();

    private boolean valid = true;

    private final boolean purge;
    private final DecorationLike decoration;

    private Runnable closeCallback;
    private Runnable openCallback;

    @FunctionalInterface
    public interface SimpleContainerListener {
        void onChange(Container container);
    }
    private final List<SimpleContainerListener> listeners = new ArrayList<>();

    public FilamentContainer(DecorationLike decoration, int size, boolean purge) {
        super(size);

        this.decoration = decoration;
        this.purge = purge;
    }

    @Override
    public @NotNull List<ContainerUser> getEntitiesWithContainerOpen() {
        return menus;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return this.valid && (decoration == null || !decoration.isRemoved());
    }

    @Override
    public boolean canTakeItem(@NonNull Container target, int slot, @NonNull ItemStack stack) {
        return this.valid;
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return this.valid && (decoration == null || !decoration.isRemoved()) && stack.getCount() <= getMaxStackSize(slot) - getItem(slot).getCount();
    }

    public int getMaxStackSize(int slot) {
        return getMaxStackSize();
    }

    public void setValid(boolean valid) {
        if (!valid) {
            for (ContainerUser entity : this.menus) {
                if (entity instanceof ServerPlayer player) player.closeContainer();
            }
        }
        this.valid = valid;
    }

    public boolean hasViewers() {
        return !this.menus.isEmpty();
    }

    @Override
    public void startOpen(ContainerUser containerUser) {
        if (containerUser.getLivingEntity() instanceof ServerPlayer serverPlayer) this.unpackLootTable(serverPlayer);
        super.startOpen(containerUser);

        if (!containerUser.getLivingEntity().isSpectator() && this.menus.isEmpty() && this.openCallback != null) {
            this.openCallback.run();
        }

        this.menus.add(containerUser);
    }

    @Override
    public void stopOpen(@NonNull ContainerUser containerUser) {
        super.stopOpen(containerUser);

        this.menus.remove(containerUser);

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
        if (decoration != null) this.unpackLootTable(null);
        return super.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int n) {
        if (decoration != null) this.unpackLootTable(null);
        return super.getItem(n);
    }

    @Override
    public @NotNull ItemStack removeItem(int n, int n2) {
        if (decoration != null) this.unpackLootTable(null);
        return super.removeItem(n, n2);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int n) {
        if (decoration != null) this.unpackLootTable(null);
        return super.removeItemNoUpdate(n);
    }

    @Override
    public void setItem(int n, @NonNull ItemStack itemStack) {
        if (decoration != null) this.unpackLootTable(null);
        super.setItem(n, itemStack);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        if (decoration != null) this.unpackLootTable(null);
        return this.items;
    }

    public DecorationLike getDecoration() {
        return decoration;
    }

    @Nullable
    public DecorationBlockEntity getBlockEntity() {
        return decoration instanceof DecorationBlockEntity blockEntity ? blockEntity : null;
    }

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        var containerLike = DecorationData.getFirstContainer(decoration);
        if (containerLike != null)
            return containerLike.getLootTable();
        return null;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
        var containerLike = DecorationData.getFirstContainer(decoration);
        if (containerLike != null) containerLike.setLootTable(resourceKey);
    }

    @Override
    public long getLootTableSeed() {
        var containerLike = DecorationData.getFirstContainer(decoration);
        if (containerLike != null)
            return containerLike.getLootTableSeed();
        return 0;
    }

    @Override
    public void setLootTableSeed(long l) {
        var containerLike = DecorationData.getFirstContainer(decoration);
        if (containerLike != null)
            containerLike.setLootTableSeed(l);
    }

    @Override
    public @NotNull BlockPos getBlockPos() {
        return decoration.getBlockPos();
    }

    @Override
    public @Nullable Level getLevel() {
        return decoration.getLevel();
    }

    public static boolean isPickUpContainer(Container container) {
        ContainerLike containerLike;
        return container instanceof FilamentContainer filamentContainer
                && filamentContainer.getDecoration() != null
                && (containerLike = DecorationData.getFirstContainer(filamentContainer.getDecoration())) != null
                && containerLike.canPickUp();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (SimpleContainerListener listener : listeners) {
            listener.onChange(this);
        }
    }

    public void addListener(SimpleContainerListener o) {
        listeners.add(o);
    }

    public void removeListener(SimpleContainerListener o) {
        listeners.remove(o);
    }
}
