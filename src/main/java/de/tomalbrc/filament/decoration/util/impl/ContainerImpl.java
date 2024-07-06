package de.tomalbrc.filament.decoration.util.impl;

import de.tomalbrc.filament.data.behaviours.decoration.Container;
import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

/**
 * Container implementation
 * @param name
 * @param container
 * @param menuType
 * @param purge
 */
public record ContainerImpl(
        String name,
        FilamentContainer container,
        MenuType<?> menuType,

        boolean purge
) {
    public void write(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put("Container", new CompoundTag().merge(ContainerHelper.saveAllItems(new CompoundTag(), this.container.items, provider)));
    }

    public void read(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag compoundTag2 = compoundTag.getCompound("Container");
        if (!compoundTag2.isEmpty()) {
            ContainerHelper.loadAllItems(compoundTag2, container.items, provider);
        }
    }

    public static MenuType<?> getMenuType(@NotNull Container containerData) {
        return switch (containerData.size) {
            case 9 -> MenuType.GENERIC_9x1;
            case 2 * 9 -> MenuType.GENERIC_9x2;
            case 3 * 9 -> MenuType.GENERIC_9x3;
            case 4 * 9 -> MenuType.GENERIC_9x4;
            case 5 * 9 -> MenuType.GENERIC_9x5;
            case 6 * 9 -> MenuType.GENERIC_9x6;
            case 5 -> MenuType.HOPPER;
            default ->
                    throw new IllegalStateException("Unexpected container size: " + containerData.name + " " + containerData.size);
        };
    }
}
