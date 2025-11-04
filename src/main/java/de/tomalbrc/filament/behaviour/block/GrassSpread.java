package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.data.properties.RangedValue;
import de.tomalbrc.filament.data.properties.RangedVector3f;
import de.tomalbrc.filament.mixin.behaviour.grass_spread.SpreadingSnowyDirtBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class GrassSpread implements BlockBehaviour<GrassSpread.Config>, SimpleWaterloggedBlock {
    private final Config config;

    public GrassSpread(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public GrassSpread.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (config.canDecay && !SpreadingSnowyDirtBlockAccessor.invokeCanBeGrass(blockState, serverLevel, blockPos)) {
            serverLevel.setBlockAndUpdate(blockPos, config.decayBlockState);
        } else {
            if (config.requiredBrightness.isInRange(serverLevel.getMaxLocalRawBrightness(blockPos.above()))) {
                BlockState defaultBlockState = blockState.getBlock().defaultBlockState();

                for (int i = 0; i < config.attemptsPerTick.getValue(blockState); ++i) {
                    var dim = config.spreadDimensions.getValue(blockState).random(randomSource);
                    var off = config.spreadOffset.getValue(blockState);
                    var posOffset = dim.add(off);

                    BlockPos blockPos2 = blockPos.offset(BlockPos.containing(posOffset.x, posOffset.y, posOffset.z));
                    BlockState blockState2 = serverLevel.getBlockState(blockPos2);
                    var canReplace = false;
                    for (Block block : config.propagatesToBlocks) {
                        canReplace |= blockState2.is(block);
                        if (canReplace) break;
                    }
                    if (!canReplace && config.propagatesToBlocks != null && !config.propagatesToBlockTags.isEmpty()) {
                        for (ResourceLocation tag : config.propagatesToBlockTags) {
                            canReplace |= blockState2.is(TagKey.create(Registries.BLOCK, tag));
                            if (canReplace) break;
                        }
                    }

                    if (canReplace && SpreadingSnowyDirtBlockAccessor.invokeCanPropagate(defaultBlockState, serverLevel, blockPos2)) {
                        serverLevel.setBlockAndUpdate(blockPos2, defaultBlockState);
                    }
                }
            }
        }
    }

    public static class Config {
        public BlockStateMappedProperty<RangedVector3f> spreadDimensions = BlockStateMappedProperty.of(new RangedVector3f(new RangedValue(0, 3), new RangedValue(0, 5), new RangedValue(0, 3)));
        public BlockStateMappedProperty<Vector3f> spreadOffset = BlockStateMappedProperty.of(new Vector3f(-1, -3, -1));
        public BlockStateMappedProperty<Integer> attemptsPerTick = BlockStateMappedProperty.of(4);
        public RangedValue requiredBrightness = new RangedValue(9, 15);
        public boolean canDecay = true;
        public BlockState decayBlockState = Blocks.DIRT.defaultBlockState();
        public List<Block> propagatesToBlocks = List.of(Blocks.DIRT);
        public List<ResourceLocation> propagatesToBlockTags = List.of();
    }
}