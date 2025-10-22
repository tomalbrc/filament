package de.tomalbrc.filament.mixin.behaviour.fire;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.behaviour.block.Fire;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @Inject(method = "getState", at = @At(value = "RETURN"), cancellable = true)
    private static void filament$getState(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<BlockState> cir, @Local BlockState blockState) {
        Block block = Fire.getMaterialFire(blockState);
        if (block != null) {
            cir.setReturnValue(block.defaultBlockState());
        }
    }
}
