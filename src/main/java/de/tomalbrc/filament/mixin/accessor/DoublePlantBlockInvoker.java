package de.tomalbrc.filament.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DoublePlantBlock.class)
public interface DoublePlantBlockInvoker {
    @Invoker
    static void invokePreventDropFromBottomPart(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AssertionError();
    }
}
