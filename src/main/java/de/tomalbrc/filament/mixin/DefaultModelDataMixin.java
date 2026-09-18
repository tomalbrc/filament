package de.tomalbrc.filament.mixin;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.impl.DefaultModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = DefaultModelData.class, remap = false)
public class DefaultModelDataMixin {
    /**
     * Polymer iterates the fire bitmask loop as `i < 32`, but only 31
     * BlockModelType.FIRE_* values exist (0..30). Iteration 31 reads
     * STAIRS_NORTH_TOP_STRAIGHT and overwrites its USABLE_STATES entry with
     * fire states, breaking both fire and stairs rendering.
     *
     * There are two `32` constants in <clinit>: the bars loop (ordinal 0)
     * and the fire loop (ordinal 1). We only clamp the fire one.
     */
    @ModifyConstant(
        method = "<clinit>",
        constant = @Constant(intValue = 32, ordinal = 1)
    )
    private static int filament$clampFireLoop(int original) {
        try {
            BlockModelType.valueOf("FIRE_NORTH_EAST_SOUTH_WEST_UP");
            return original;
        } catch (IllegalArgumentException e) {
            return 31;
        }
    }
}