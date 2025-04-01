package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(
            method = "setBlockEntity",
            at = @At("TAIL")
    )
    private void filament$filamentDecorationInit(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof BlockEntityWithElementHolder decorationBlockEntity)
            decorationBlockEntity.attach((LevelChunk)(Object) this);
    }
//
//    @Inject(
//            method = {"setBlockState"},
//            at = {@At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
//                    ordinal = 0
//            )}
//    )
//    private void filament$virtualBreakParticle(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
//        var x = BlockBoundAttachment.get(LevelChunk.class.cast(this), pos);
//        if (x != null) {
//            if (x.getBlockState() != state) {
//                if ((flags & Block.UPDATE_SKIP_ALL_SIDEEFFECTS) > 0 || (flags & Block.UPDATE_SUPPRESS_DROPS) > 0) {
//
//                }
//            } else {
//                x.setBlockState(state);
//            }
//        }
//
//    }
}
