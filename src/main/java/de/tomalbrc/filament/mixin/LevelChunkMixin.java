package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.BlockEntityWithElementHolder;
import de.tomalbrc.filament.registry.OxidizableRegistry;
import de.tomalbrc.filament.registry.StrippableRegistry;
import de.tomalbrc.filament.registry.WaxableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (blockEntity instanceof BlockEntityWithElementHolder blockEntityWithElementHolder) {
            Filament.SERVER.schedule(new TickTask(0, () -> blockEntityWithElementHolder.attach((LevelChunk)(Object) this)));
        }
    }

    @Inject(
            method = "setBlockEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V")
    )
    private void filament$filamentDecorationCopyData(BlockEntity blockEntity, CallbackInfo ci, @Local(ordinal = 1) BlockEntity blockEntity2) {
        if (blockEntity2 instanceof DecorationBlockEntity old && old.replaceable() && blockEntity instanceof DecorationBlockEntity fresh) {
            if (filament$replace(old.getBlock(), fresh.getBlock())) {
                CompoundTag compoundTag = old.saveWithoutMetadata(Filament.SERVER.registryAccess());

                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Filament.LOGGER)) {
                    fresh.loadAdditional(TagValueInput.create(scopedCollector, Filament.SERVER.registryAccess(), compoundTag));
                    fresh.setChanged();
                } catch (Throwable throwable) {
                    Filament.LOGGER.error("Failed to copy data for decoration {} for block {} at position {} to new decoration {}", old.getType(), old.getBlockPos(), old.getBlockState(), fresh.getType(), throwable);
                }
            }
        }
    }

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void filament$avoidRemovalOxidation(LevelChunk instance, BlockPos blockPos, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState, @Local(ordinal = 1) BlockState blockStateOld) {
        var oxiDeco = !blockStateOld.isAir() && blockStateOld.getBlock() instanceof DecorationBlock && blockState.getBlock() instanceof DecorationBlock && filament$replace(blockStateOld.getBlock(), blockState.getBlock());
        if (!oxiDeco) {
            original.call(instance, blockPos);
        }
    }

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false))
    private void filament$avoidRemovalOxidationWarn(Logger instance, String s, Object[] objects, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState, @Local(ordinal = 1) BlockState blockStateOld, @Local(argsOnly = true) BlockPos blockPos) {
        var oxiDeco = !blockStateOld.isAir() && blockStateOld.getBlock() instanceof DecorationBlock && blockState.getBlock() instanceof DecorationBlock && filament$replace(blockStateOld.getBlock(), blockState.getBlock());
        if (!oxiDeco) {
            original.call(instance, s, objects);
        }
    }

    @Unique
    private boolean filament$replace(Block old, Block fresh) {
        return OxidizableRegistry.sameOxidizable(fresh, old) || StrippableRegistry.get(old) == fresh || WaxableRegistry.getPrevious(fresh) == old;
    }
}
