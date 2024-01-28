package de.tomalbrc.filament.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameAccessor {
    @Accessor
    static EntityDataAccessor<Integer> getDATA_ROTATION() {
        throw new UnsupportedOperationException();
    }
}
