package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.data.AbstractBlockData;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.data.properties.BlockProperties;
import de.tomalbrc.filament.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.Optional;
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
        this.treeGrower = new TreeGrower(
                name,
                config.secondaryChance,
                Optional.ofNullable(key(config.megaTree)),
                Optional.ofNullable(key(config.secondaryMegaTree)),
                Optional.ofNullable(key(config.tree)),
                Optional.ofNullable(key(config.secondaryTree)),
                Optional.ofNullable(key(config.flowers)),
                Optional.ofNullable(key(config.secondaryFlowers))
        );
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

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> key(ResourceLocation resourceLocation) {
        if (resourceLocation == null)
            return null;

        return ResourceKey.create(Registries.CONFIGURED_FEATURE, resourceLocation);
    }

    @Override
    @NotNull
    public Sapling.Config getConfig() {
        return this.config;
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
            this.treeGrower.growTree(serverLevel, serverLevel.getChunkSource().getGenerator(), blockPos, blockState, randomSource);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        if (level instanceof ServerLevel serverLevel && blockState.getBlock().isFilamentBlock() && !(blockState.getBlock().asFilamentBlock().getPolymerBlockState(blockState, PacketContext.create()).getBlock() instanceof BonemealableBlock)) {
            BlockUtil.handleBoneMealEffects(serverLevel, blockPos);
        }
        return level.random.nextFloat() < config.bonemealGrowthChance;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        this.grow(serverLevel, blockPos, blockState, randomSource);
    }

    public static class Config {
        int minLightLevel = 9;
        float randomTickGrowthChance = 0.15f;
        float bonemealGrowthChance = 0.45f;
        float secondaryChance = 0.f;
        ResourceLocation megaTree = null;
        ResourceLocation secondaryMegaTree = null;
        ResourceLocation tree = null;
        ResourceLocation secondaryTree = null;
        ResourceLocation flowers = null;
        ResourceLocation secondaryFlowers = null;
    }
}
