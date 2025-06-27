package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Decoration containers, such as chests, or just drawers etc.
 */
public class Container implements DecorationBehaviour<Container.Config> {
    public final FilamentContainer container;

    private final Config config;

    public Container(Config config) {
        this.config = config;
        this.container = new FilamentContainer(config.size, config.purge);
    }

    @Override
    @NotNull
    public Container.Config getConfig() {
        return config;
    }

    public static class Config {
        /**
         * The name displayed in the container UI
         */
        public String name;

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
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        if (this.config.canPickup)
            Objects.requireNonNull(blockEntity.getItem().get(DataComponents.CONTAINER)).copyInto(container.items);
    }

    @Override
    public void write(ValueOutput output, DecorationBlockEntity decorationBlockEntity) {
        ContainerHelper.saveAllItems(output.child("Container"), this.container.items);
    }

    @Override
    public void read(ValueInput input, DecorationBlockEntity decorationBlockEntity) {
        input.child("Container").ifPresent(x -> ContainerHelper.loadAllItems(input, container.items));
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!player.isSecondaryUseActive()) {
            Component containerName = Component.literal(config.name == null ? "filament container" : config.name);
            if (this.container.getContainerSize() % 9 == 0) {
                player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) -> new ChestMenu(getMenuType(), i, playerInventory, container, container.getContainerSize() / 9), containerName));
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
            for (ItemStack itemStack : container.items) {
                if (itemStack.isEmpty()) continue;
                Util.spawnAtLocation(decorationBlockEntity.getLevel(), decorationBlockEntity.getBlockPos().getCenter(), itemStack);
            }
        }
    }

    @Override
    public void modifyDrop(DecorationBlockEntity blockEntity, ItemStack itemStack) {
        if (config.canPickup) {
            itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
        }
    }

    public MenuType<?> getMenuType() {
        return switch (config.size) {
            case 9 -> MenuType.GENERIC_9x1;
            case 2 * 9 -> MenuType.GENERIC_9x2;
            case 3 * 9 -> MenuType.GENERIC_9x3;
            case 4 * 9 -> MenuType.GENERIC_9x4;
            case 5 * 9 -> MenuType.GENERIC_9x5;
            case 6 * 9 -> MenuType.GENERIC_9x6;
            case 5 -> MenuType.HOPPER;
            default ->
                    throw new IllegalStateException("Unexpected container size: " + config.name + " " + config.size);
        };
    }
}
