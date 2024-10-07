package de.tomalbrc.filament.mixin.behaviour;

import de.tomalbrc.filament.registry.OxidizableRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WeatheringCopper.class)
public interface WeatheringCopperMixin {
    @Inject(method = "getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private static void filament$onGetPrevious(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (OxidizableRegistry.hasPrevious(blockState.getBlock())) {
            var v = OxidizableRegistry.getPrevious(blockState.getBlock()).withPropertiesOf(blockState);
            cir.setReturnValue(Optional.of(v));
        }
    }

    @Inject(method = "getFirst(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private static void filament$onFirst(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (OxidizableRegistry.hasPrevious(blockState.getBlock())) {
            Block prev = OxidizableRegistry.getPrevious(blockState.getBlock());
            while (WeatheringCopper.getPrevious(prev).isPresent()) {
                prev = WeatheringCopper.getPrevious(prev).get();
            }
            cir.setReturnValue(Optional.of(prev.withPropertiesOf(blockState)));
        }
    }

    @Inject(method = "getNext(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void filament$onGetNext(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (OxidizableRegistry.hasNext(blockState.getBlock())) {
            var v = OxidizableRegistry.getNext(blockState.getBlock()).withPropertiesOf(blockState);
            cir.setReturnValue(Optional.of(v));
        }
    }
}
