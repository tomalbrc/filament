package de.tomalbrc.filament.util.mixin;

import net.minecraft.nbt.CompoundTag;

public interface BlockEntityWithDataTransfer {
    CompoundTag getDataAndClear();

    void setData(CompoundTag compoundTag);
}
