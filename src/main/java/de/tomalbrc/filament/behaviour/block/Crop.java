package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Crop implements BlockBehaviour<Crop.Config> {
    public static final IntegerProperty[] AGES = {
            IntegerProperty.create("age", 0,1),
            IntegerProperty.create("age", 0,2),
            IntegerProperty.create("age", 0,3),
            IntegerProperty.create("age", 0,4),
            IntegerProperty.create("age", 0,5),
            IntegerProperty.create("age", 0,6),
            IntegerProperty.create("age", 0,7),
            IntegerProperty.create("age", 0,8),
            IntegerProperty.create("age", 0,9),
            IntegerProperty.create("age", 0,10),
            IntegerProperty.create("age", 0,11),
            IntegerProperty.create("age", 0,12),
            IntegerProperty.create("age", 0,13),
            IntegerProperty.create("age", 0,14),
            IntegerProperty.create("age", 0,15)
    };

    private final Config config;

    public Crop(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Crop.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGES[Math.min(0, config.max-1)]);
        return true;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        var belowState = levelReader.getBlockState(blockPos.below());
        if (config.survivesOnBocks != null && config.survivesOnBocks.contains(belowState.getBlock()))
            return true;
        if (config.survivesOnBlocksWithTags != null) {
            for (ResourceLocation tag : config.survivesOnBlocksWithTags) {
                var tagKey = TagKey.create(Registries.BLOCK, tag);
                if (belowState.is(tagKey))
                    return true;
            }
        }
        return false;
    }

    public static class Config {
        public int max = 4;

        public List<Block> survivesOnBocks;
        public List<ResourceLocation> survivesOnBlocksWithTags;
    }
}
