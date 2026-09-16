package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.BlockUtil;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;

public class Sapling implements BlockBehaviour<Sapling.Config>, BonemealableBlock {
    private final Config config;

    private TreeGrower treeGrower;

    public Sapling(Config config) {
        this.config = config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        BlockBehaviour.super.init(item, block, behaviourHolder);

        String name = UUID.randomUUID().toString();

        // Build WeightedList objects from the config's Identifier fields.
        // A null Identifier means an empty WeightedList for that category.
        WeightedList<ResourceKey<Feature>> trees = buildWeightedList(config.tree, config.secondaryTree, config.secondaryChance);
        WeightedList<ResourceKey<Feature>> megaTrees = buildWeightedList(config.megaTree, config.secondaryMegaTree, config.secondaryChance);
        WeightedList<ResourceKey<Feature>> flowerTrees = buildWeightedList(config.flowers, config.secondaryFlowers, config.secondaryChance);

        this.treeGrower = new TreeGrower(
                name,
                trees,
                megaTrees,
                flowerTrees,
                key(config.tree)
        );
    }

    private static WeightedList<ResourceKey<Feature>> buildWeightedList(@Nullable Identifier primary, @Nullable Identifier secondary, float secondaryChance) {
        ResourceKey<Feature> primaryKey = key(primary);
        ResourceKey<Feature> secondaryKey = key(secondary);

        if (primaryKey == null && secondaryKey == null) {
            return WeightedList.of();
        }
        if (primaryKey == null) {
            return WeightedList.of(secondaryKey);
        }
        if (secondaryKey == null || secondaryChance <= 0.0f) {
            return WeightedList.of(primaryKey);
        }

        int secondaryWeight = Math.max(1, Math.round(secondaryChance * 100.0f));
        int primaryWeight = 100 - secondaryWeight;

        return WeightedList.of(
                new Weighted<>(primaryKey, primaryWeight),
                new Weighted<>(secondaryKey, secondaryWeight)
        );
    }

    @Nullable
    private static ResourceKey<Feature> key(@Nullable Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        return ResourceKey.create(Registries.FEATURE, identifier);
    }

    @Override
    @NotNull
    public Sapling.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean modifyStateMap(Map<BlockState, BlockData.BlockStateMeta> map, AbstractBlockData<? extends BlockProperties> blockData) {
        // support for only 1 model if wanted
        if (map.size() == 1) {
            var entry = map.entrySet().iterator().next();
            map.put(entry.getKey().cycle(BlockStateProperties.STAGE), entry.getValue());
        }
        return true;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.STAGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.getMaxLocalRawBrightness(blockPos.above()) >= config.minLightLevel && randomSource.nextFloat() < config.randomTickGrowthChance) {
            this.grow(serverLevel, blockPos, blockState, randomSource);
        }
    }

    private void grow(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
        if (blockState.getValue(BlockStateProperties.STAGE) == 0) {
            serverLevel.setBlock(blockPos, blockState.cycle(BlockStateProperties.STAGE), Block.UPDATE_CLIENTS);
        } else {
            this.treeGrower.growTree(
                    serverLevel,
                    serverLevel.getChunkSource().getGenerator(),
                    blockPos,
                    blockState,
                    randomSource
            );
        }
    }

    @Override
    public boolean isValidBonemealTarget(@NonNull LevelReader levelReader, @NonNull BlockPos blockPos, @NonNull BlockState blockState, @NonNull BonemealSource bonemealSource) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(@NonNull Level level, @NonNull RandomSource randomSource,
                                     @NonNull BlockPos blockPos, @NonNull BlockState blockState,
                                     @NonNull BonemealSource bonemealSource) {
        if (level instanceof ServerLevel serverLevel
                && blockState.getBlock().isFilamentBlock()
                && !(blockState.getBlock().asFilamentBlock()
                .getPolymerBlockState(blockState, PacketContext.get())
                .getBlock() instanceof BonemealableBlock)) {
            BlockUtil.handleBoneMealEffects(serverLevel, blockPos);
        }
        return level.getRandom().nextFloat() < config.bonemealGrowthChance;
    }

    @Override
    public void performBonemeal(@NonNull ServerLevel serverLevel, @NonNull RandomSource randomSource,
                                @NonNull BlockPos blockPos, @NonNull BlockState blockState,
                                @NonNull BonemealSource bonemealSource) {
        this.grow(serverLevel, blockPos, blockState, randomSource);
    }

    public static class Config {
        int minLightLevel = 9;
        float randomTickGrowthChance = 0.15f;
        float bonemealGrowthChance = 0.45f;
        float secondaryChance = 0.f;
        Identifier megaTree = null;
        Identifier secondaryMegaTree = null;
        Identifier tree = null;
        Identifier secondaryTree = null;
        Identifier flowers = null;
        Identifier secondaryFlowers = null;
    }
}