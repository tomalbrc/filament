package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.DecorationRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    @Shadow protected abstract void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state);

    @Inject(method = "canPassThroughWall(Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void filament$customCanPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, CallbackInfoReturnable<Boolean> cir) {
        if (DecorationRegistry.isDecoration(blockState2) || DecorationRegistry.isDecoration(blockState)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean filament$passes(BlockState blockState) {
        if (DecorationRegistry.isDecoration(blockState) && (!filament$isSolid((DecorationBlock) blockState.getBlock()) || filament$isWaterloggable(blockState)))
            return this.filament$canFlowThrough(blockState);
        return false;
    }

    @Inject(method = "canMaybePassThrough", at = @At("RETURN"), cancellable = true)
    private void filament$canMaybePassThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        // pass-thu but only non-waterloggable blocks and non-solid
        if (filament$passes(blockState2)) cir.setReturnValue(true);
    }

    @Inject(method = "canPassThrough", at = @At("RETURN"), cancellable = true)
    private void filament$canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (filament$passes(blockState)) cir.setReturnValue(true);
        if (filament$passes(blockState2)) cir.setReturnValue(true);
    }

    @Inject(method = "spreadTo", at = @At(value = "HEAD"))
    protected void filament$spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState, CallbackInfo ci) {
        if (DecorationRegistry.isDecoration(blockState) && !filament$isSolid((DecorationBlock) blockState.getBlock())) {
            if (levelAccessor.getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                decorationBlockEntity.destroyStructure(true);
            }
        }
    }

    @Unique
    private boolean filament$canFlowThrough(BlockState blockState) {
        DecorationBlock decorationBlock = (DecorationBlock) blockState.getBlock();
        if (decorationBlock.getDecorationData() != null) {
            return !decorationBlock.getDecorationData().hasBlocks();
        }

        return false;
    }

    @Unique
    private static boolean filament$isWaterloggable(BlockState decorationBlock) {
        return decorationBlock.hasProperty(BlockStateProperties.WATERLOGGED);
    }

    @Unique
    private static boolean filament$isSolid(DecorationBlock decorationBlock) {
        if (decorationBlock.getDecorationData() != null) {
            return decorationBlock.getDecorationData().properties().solid;
        }

        return false;
    }
}
