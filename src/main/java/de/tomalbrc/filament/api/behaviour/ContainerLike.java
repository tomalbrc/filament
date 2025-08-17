package de.tomalbrc.filament.api.behaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

public interface ContainerLike {
    Component customName();

    @Nullable
    Container container();

    boolean showCustomName();

    boolean hopperDropperSupport();
}
