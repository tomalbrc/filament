package de.tomalbrc.filament.api.behaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface ContainerLike {
    Component customName();

    @Nullable
    Container container();

    boolean showCustomName();

    boolean hopperDropperSupport();

    void setLootTable(@Nullable ResourceKey<LootTable> resourceKey);

    @Nullable ResourceKey<LootTable> getLootTable();

    void setLootTableSeed(long l);

    long getLootTableSeed();
}
