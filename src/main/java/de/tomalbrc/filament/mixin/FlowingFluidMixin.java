package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.config.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationBlockEntity;
import de.tomalbrc.filament.registry.BlockRegistry;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @Inject(method = "canSpreadTo", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    private void canSpreadTo(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (blockState2.is(BlockRegistry.DECORATION_BLOCK)) {
            boolean isWaterloggable = this.isWaterloggable(blockGetter, blockPos2, blockState2) && direction != Direction.DOWN;
            boolean isSolid = this.isSolid(blockGetter, blockPos2, blockState2) && direction != Direction.DOWN;
            cir.setReturnValue(isWaterloggable || !isSolid);
        }
    }

    @Inject(method = "spreadTo", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState, CallbackInfo ci) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) && !isWaterloggable(levelAccessor, blockPos, blockState) && !isSolid(levelAccessor, blockPos, blockState)) {
            if (levelAccessor.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                decorationBlockEntity.destroyStructure(true);
            }

            // let it overflow with liquid
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
        }
    }

    @Inject(method = "canPassThrough", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    private void canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        // pass-thu but only non-waterloggable blocks and non-solid
        if (blockState2.is(BlockRegistry.DECORATION_BLOCK) && !isWaterloggable(blockGetter, blockPos2, blockState2) && !isSolid(blockGetter, blockPos2, blockState2))
            cir.setReturnValue(this.canFlowThrough(blockGetter, blockPos2, blockState2));
    }
    @Inject(method = "canPassThroughWall", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("TAIL"), cancellable = true)
    private void canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, CallbackInfoReturnable<Boolean> cir, Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap, Block.BlockStatePairKey blockStatePairKey, VoxelShape voxelShape, VoxelShape voxelShape2, boolean bl) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) || blockState2.is(BlockRegistry.DECORATION_BLOCK))
            cir.setReturnValue(true);
    }

    private boolean canFlowThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK)) {
            if (blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity && decorationBlockEntity.getDecorationData() != null && decorationBlockEntity.getDecorationData().properties() != null) {
                return decorationBlockEntity.getDecorationData().blocks() == null && !decorationBlockEntity.getDecorationData().properties().waterloggable && !decorationBlockEntity.getDecorationData().properties().solid;
            }
        }

        return false;
    }

    private boolean isWaterloggable(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) && blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity && decorationBlockEntity.getDecorationData() != null && decorationBlockEntity.getDecorationData().properties() != null) {
            return decorationBlockEntity.getDecorationData().properties().waterloggable;
        }

        return false;
    }

    private boolean isSolid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        if (blockState.is(BlockRegistry.DECORATION_BLOCK) && blockGetter.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity && decorationBlockEntity.getDecorationData() != null && decorationBlockEntity.getDecorationData().properties() != null) {
            return decorationBlockEntity.getDecorationData().properties().solid;
        }

        return false;
    }
}
