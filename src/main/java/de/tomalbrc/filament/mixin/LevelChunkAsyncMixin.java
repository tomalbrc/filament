package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.block.SimpleBlock;
import de.tomalbrc.filament.util.AsyncBlockTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkAsyncMixin extends ChunkAccess {
    @Shadow @Final Level level;

    @Shadow public abstract Level getLevel();

    public LevelChunkAsyncMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private void filament$removeOldAsyncTicker(BlockPos blockPos, BlockState blockState, int i, CallbackInfoReturnable<BlockState> cir) {
        var x = AsyncBlockTicker.get(blockPos);
        if (x != null) {
            if (x != blockState.getBlock()) {
                AsyncBlockTicker.remove(blockPos);
            } else if (blockState.getBlock() instanceof SimpleBlock simpleBlock && getLevel() instanceof ServerLevel serverLevel) {
                AsyncBlockTicker.add(blockPos, simpleBlock, serverLevel);
            }
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", ordinal = 1, shift = At.Shift.BEFORE))
    private void filament$addAsyncTicker(BlockPos blockPos, BlockState blockState, int i, CallbackInfoReturnable<BlockState> cir) {
        var x = AsyncBlockTicker.get(blockPos);
        if (x == null && this.level instanceof ServerLevel serverLevel && blockState.getBlock() instanceof SimpleBlock simpleBlock && filament$isAsyncTickingBlock(simpleBlock)) {
            AsyncBlockTicker.add(blockPos, simpleBlock, serverLevel);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("TAIL"))
    private void filament$loadAsyncTicker(Level level, ChunkPos chunkPos, UpgradeData upgradeData, LevelChunkTicks levelChunkTicks, LevelChunkTicks levelChunkTicks2, long l, LevelChunkSection[] levelChunkSections, LevelChunk.PostLoadProcessor postLoadProcessor, BlendingData blendingData, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            var sections = this.getSections();
            for (int i = 0; i < sections.length; i++) {
                var section = sections[i];
                if (section != null && !section.hasOnlyAir()) {
                    var container = section.getStates();
                    if (container.maybeHas(blockState -> blockState.getBlock() instanceof SimpleBlock simpleBlock && filament$isAsyncTickingBlock(simpleBlock))) {
                        BlockState state;
                        for (byte x = 0; x < 16; x++) {
                            for (byte z = 0; z < 16; z++) {
                                for (byte y = 0; y < 16; y++) {
                                    state = container.get(x, y, z);
                                    if (state.getBlock() instanceof SimpleBlock simpleBlock && simpleBlock.hasData()) {
                                        if (filament$isAsyncTickingBlock(simpleBlock)) {
                                            var blockPos = chunkPos.getBlockAt(x, this.getSectionYFromSectionIndex(i) * 16 + y, z);
                                            AsyncBlockTicker.add(blockPos, simpleBlock, serverLevel);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private static boolean filament$isAsyncTickingBlock(SimpleBlock simpleBlock) {
        return simpleBlock.data().behaviour().test(behaviourType -> AsyncTickingBlockBehaviour.class.isAssignableFrom(behaviourType.type()));
    }
}
