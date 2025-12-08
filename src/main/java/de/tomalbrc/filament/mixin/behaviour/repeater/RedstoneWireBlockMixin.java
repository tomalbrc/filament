package de.tomalbrc.filament.mixin.behaviour.repeater;

import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.block.Repeater;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSignalSource()Z"), cancellable = true)
    private static void filament$redstoneConnect(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock().isFilamentBlock()) {
            var repeater = state.getBlock().get(Behaviours.REPEATER);
            if (repeater != null) {
                cir.setReturnValue(state.getValue(Repeater.FACING) == direction);
            }
        }
    }
}
