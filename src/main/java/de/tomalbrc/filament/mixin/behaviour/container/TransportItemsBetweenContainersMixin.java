package de.tomalbrc.filament.mixin.behaviour.container;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransportItemsBetweenContainers.TransportItemTarget.class)
public class TransportItemsBetweenContainersMixin {
    @Inject(method = "getBlockEntityContainer", at = @At("RETURN"), cancellable = true)
    private static void filament$getContainer(BlockEntity blockEntity, BlockState blockState, Level level, BlockPos blockPos, CallbackInfoReturnable<Container> cir) {
        if (blockEntity instanceof DecorationBlockEntity decorationBlockEntity) {
            var res = DecorationData.getFirstContainer(decorationBlockEntity);
            if (res != null) {
                cir.setReturnValue(res.container());
            }
        }
    }
}
