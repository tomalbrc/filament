package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChestBlock.class)
public interface ChestBlockInvoker {
    @Invoker("isCatSittingOnChest")
    static boolean isCatSittingOnChest(LevelAccessor levelAccessor, BlockPos blockPos) {
        throw new AssertionError();
    }
}