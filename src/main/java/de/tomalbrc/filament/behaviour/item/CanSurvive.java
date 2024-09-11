package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CanSurvive implements BlockBehaviour<CanSurvive.Config> {
    private final Config config;

    public CanSurvive(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public CanSurvive.Config getConfig() {
        return this.config;
    }

    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        var belowState = levelReader.getBlockState(blockPos.below());
        if (config.blocks != null && config.blocks.contains(belowState.getBlock()))
            return true;
        if (config.tags != null) {
            for (ResourceLocation tag : config.tags) {
                var tagKey = TagKey.create(Registries.BLOCK, tag);
                if (belowState.is(tagKey))
                    return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return !blockState.canSurvive(levelAccessor, blockPos) ? Blocks.AIR.defaultBlockState() : blockState;
    }

    public static class Config {
        public List<Block> blocks;
        public List<ResourceLocation> tags;
    }
}
