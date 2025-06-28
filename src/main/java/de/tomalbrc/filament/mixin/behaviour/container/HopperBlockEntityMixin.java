package de.tomalbrc.filament.mixin.behaviour.container;

import de.tomalbrc.filament.behaviour.Behaviours;
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

import java.util.Objects;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "getBlockContainer", at = @At(value = "HEAD"), cancellable = true)
    private static void filament$customContainerSupport(Level level, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<Container> cir) {
        if (blockState.getBlock() instanceof ComplexDecorationBlock complexDecorationBlock && complexDecorationBlock.has(Behaviours.CONTAINER) && Objects.requireNonNull(complexDecorationBlock.get(Behaviours.CONTAINER)).getConfig().hopperDropperSupport) {
            DecorationBlockEntity blockEntity = (DecorationBlockEntity) level.getBlockEntity(blockPos);
            assert blockEntity != null;
            cir.setReturnValue(Objects.requireNonNull(blockEntity.getMainBlockEntity().get(Behaviours.CONTAINER)).container);
        }
    }
}
