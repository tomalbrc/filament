package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameAccessor {
    @Accessor
    static EntityDataAccessor<@NotNull Integer> getDATA_ROTATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<@NotNull ItemStack> getDATA_ITEM() {
        throw new UnsupportedOperationException();
    }
}
