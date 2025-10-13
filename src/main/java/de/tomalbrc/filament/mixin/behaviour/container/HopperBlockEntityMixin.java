package de.tomalbrc.filament.mixin.behaviour.container;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.ComplexDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "getBlockContainer", at = @At(value = "HEAD"), cancellable = true)
    private static void filament$customContainerSupport(Level level, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<Container> cir) {
        if (blockState.getBlock() instanceof ComplexDecorationBlock complexDecorationBlock && complexDecorationBlock.getDecorationData().isContainer()) {
            DecorationBlockEntity blockEntity = (DecorationBlockEntity) level.getBlockEntity(blockPos);
            if (blockEntity != null && (blockEntity = blockEntity.getMainBlockEntity()) != null) {
                var containerLike = DecorationData.getFirstContainer(blockEntity);
                if (containerLike != null && containerLike.hopperDropperSupport()) {
                    cir.setReturnValue(containerLike.container());
                }
            }
        }
    }
}
