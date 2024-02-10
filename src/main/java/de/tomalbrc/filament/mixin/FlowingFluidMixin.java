package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.decoration.DecorationBlockEntity;
import de.tomalbrc.filament.registry.BlockRegistry;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {
    @Inject(method = "canHoldFluid", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    private void canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) && blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
            cir.setReturnValue(decorationBlockEntity.getDecorationData().properties().waterloggable);
        }
    }
    @Inject(method = "canPassThroughWall", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    private void canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, CallbackInfoReturnable<Boolean> cir, Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap, Block.BlockStatePairKey blockStatePairKey, VoxelShape voxelShape, VoxelShape voxelShape2, boolean bl) {
        if (blockState2.is(BlockRegistry.DECORATION_BLOCK) && blockGetter.getBlockEntity(blockPos2) instanceof DecorationBlockEntity decorationBlockEntity) {
            cir.setReturnValue(decorationBlockEntity.getDecorationData().properties().waterloggable);
        }
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) && blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
            cir.setReturnValue(decorationBlockEntity.getDecorationData().properties().waterloggable);
        }
    }
}
