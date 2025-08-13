package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.ContainerLike;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Decoration containers, such as chests, or just drawers etc.
 */
public class Container implements DecorationBehaviour<Container.Config>, ContainerLike {
    public FilamentContainer container;

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
        ContainerHelper.saveAllItems(output.child("Container"), this.container.items);
    }

    @Override
    public void read(ValueInput input, DecorationBlockEntity decorationBlockEntity) {
        input.child("Container").ifPresent(x -> ContainerHelper.loadAllItems(x, container.items));
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!player.isSecondaryUseActive()) {
            Component containerName = TextUtil.formatText(config.name);
            if (this.container.getContainerSize() % 9 == 0) {
                player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> new ChestMenu(getMenuType(config.size, config.name), i, playerInventory, container, container.getContainerSize() / 9), containerName));
            } else if (this.container.getContainerSize() == 5) {
                player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> new HopperMenu(i, playerInventory, container), containerName));
            }

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
    public ItemStack getCloneItemStack(ItemStack itemStack, LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
        return itemStack;
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentGetter dataComponentGetter) {
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.container.items);
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.container.items));
    }

    public static MenuType<?> getMenuType(int size, String name) {
        return switch (size) {
            case 9 -> MenuType.GENERIC_9x1;
            case 2 * 9 -> MenuType.GENERIC_9x2;
            case 3 * 9 -> MenuType.GENERIC_9x3;
            case 4 * 9 -> MenuType.GENERIC_9x4;
            case 5 * 9 -> MenuType.GENERIC_9x5;
            case 6 * 9 -> MenuType.GENERIC_9x6;
            case 5 -> MenuType.HOPPER;
            default ->
                    throw new IllegalStateException("Unexpected container size: " + name + " " + size);
        };
    }

    @Override
    public Component customName() {
        return container.getBlockEntity().components().get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public @Nullable FilamentContainer container() {
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
    public boolean canPickup() {
        return config.canPickup;
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
    }
}
