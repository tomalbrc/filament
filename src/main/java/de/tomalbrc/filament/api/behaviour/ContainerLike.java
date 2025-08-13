package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.util.FilamentContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ContainerLike {
    Component customName();

    @Nullable
    FilamentContainer container();

    boolean showCustomName();

    boolean hopperDropperSupport();

    boolean canPickup();
}
