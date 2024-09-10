package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// For crops
@Mixin(Bee.BeeGrowCropGoal.class)
public abstract class BeeMixin {
    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    void filament$beeAddCustom(CallbackInfo ci, int i, BlockPos blockPos, BlockState blockState, Block block, LocalRef<BlockState> blockState2) {
        //blockState2.set(Blocks.STONE.defaultBlockState());
    }
}
