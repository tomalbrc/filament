package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.TextUtil;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Decoration containers, such as chests, or just drawers etc.
 */
public class Container implements DecorationBehaviour<Container.Config>, ContainerLike {
    public FilamentContainer container;
    public ResourceKey<LootTable> lootTable;
    public long lootTableSeed;

    private final Config config;

    public Container(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Container.Config getConfig() {
        return config;
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        this.container = new FilamentContainer(blockEntity, config.size, config.purge);

        var item = blockEntity.getItem();
        if (item.has(DataComponents.CONTAINER)) {
            Objects.requireNonNull(blockEntity.getItem().get(DataComponents.CONTAINER)).copyInto(container.items);
        }

        if (config.openAnimation != null) {
            container.setOpenCallback(() -> blockEntity.getOrCreateHolder().playAnimation(config.openAnimation, 2));
        }
        if (config.closeAnimation != null) {
            container.setCloseCallback(() -> blockEntity.getOrCreateHolder().playAnimation(config.closeAnimation, 2));
        }
    }

    @Override
    public void write(ValueOutput output, DecorationBlockEntity decorationBlockEntity) {
        if (!container.trySaveLootTable(output))
            ContainerHelper.saveAllItems(output.child("Container"), this.container.items);
    }

    @Override
    public void read(ValueInput input, DecorationBlockEntity decorationBlockEntity) {
        if (!container.tryLoadLootTable(input))
            input.child("Container").ifPresent(x -> ContainerHelper.loadAllItems(x, container.items));
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!player.isSecondaryUseActive()) {
            Component containerName = customName() != null && showCustomName() ? customName() : TextUtil.formatText(config.name);

            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> Util.createMenu(container, id, p), containerName));

            if (config.angerPiglins) PiglinAi.angerNearbyPiglins(player.level(), player, true);

            decorationBlockEntity.setChanged();

            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void destroy(DecorationBlockEntity decorationBlockEntity, boolean dropItem) {
        container.setValid(false);
        if (!config.canPickup) {
            Containers.dropContents(decorationBlockEntity.getLevel(), decorationBlockEntity.getBlockPos(), container);
        }
    }

    @Override
    public void modifyDrop(DecorationBlockEntity blockEntity, ItemStack itemStack) {
        if (config.canPickup) {
            itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
        }
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentGetter dataComponentGetter) {
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.container.items);

        SeededContainerLoot seededContainerLoot = dataComponentGetter.get(DataComponents.CONTAINER_LOOT);
        if (seededContainerLoot != null) {
            this.lootTable = seededContainerLoot.lootTable();
            this.lootTableSeed = seededContainerLoot.seed();
        }
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.container.items));

        if (this.lootTable != null) {
            builder.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
        }
    }

    public static MenuType<?> estimateMenuType(int size) {
        if (size <= 5) {
            return MenuType.HOPPER;
        }

        int rows = (size + 8) / 9;
        return switch (Math.min(rows, 6)) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    @Override
    public Component customName() {
        return container.getBlockEntity().components().get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public net.minecraft.world.Container container() {
        return container;
    }

    @Override
    public boolean showCustomName() {
        return config.showCustomName;
    }

    @Override
    public boolean hopperDropperSupport() {
        return config.hopperDropperSupport;
    }

    @Override
    public boolean canPickUp() {
        return config.canPickup;
    }

    @Override
    public void removeComponentsFromTag(DecorationBlockEntity decorationBlockEntity, ValueOutput valueOutput) {
        valueOutput.discard("LootTable");
        valueOutput.discard("LootTableSeed");
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
        lootTable = resourceKey;
    }

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        return lootTable;
    }

    @Override
    public void setLootTableSeed(long l) {
        lootTableSeed = l;
    }

    @Override
    public long getLootTableSeed() {
        return lootTableSeed;
    }

    public static class Config {
        /**
         * The name displayed in the container UI
         */
        public String name = "Container";

        /**
         * The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
         */
        public int size = 9;

        /**
         * Indicates whether the container's contents should be cleared when no player is viewing the inventory.
         */
        public boolean purge = false;

        /**
         * The name of the animation to play when the container is opened (if applicable).
         */
        public String openAnimation = null;

        /**
         * The name of the animation to play when the container is closed (if applicable).
         */
        public String closeAnimation = null;

        /**
         * Flag to indicate whether the container can be picked up like shulker boxes.
         */
        public boolean canPickup = false;

        /**
         * Support hopper and dropper blocks
         */
        public boolean hopperDropperSupport = true;

        public boolean showCustomName = true;

        public boolean angerPiglins = true;
    }
}
