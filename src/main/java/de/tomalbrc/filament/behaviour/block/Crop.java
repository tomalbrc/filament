package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.util.BlockUtil;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

// todo: ravager interaction, bee interaction, villager interaction!
public class Crop implements BlockBehaviour<Crop.Config>, BonemealableBlock {
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
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        BlockBehaviour.super.init(item, block, behaviourHolder);

        if (config.villagerInteraction) {
            VillagerInteractionRegistries.registerCollectable(item);
        }
    }

    @Override
    @NotNull
    public Crop.Config getConfig() {
        return this.config;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGES[Math.max(0, config.maxAge-1)]);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return hasSufficientLight(levelReader, blockPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGES[Math.max(0, config.maxAge-1)]) < this.config.maxAge-1;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.getRawBrightness(blockPos, 0) >= 9) {
            int i = this.getAge(blockState);
            if (i < this.config.maxAge-1) {
                float f = getGrowthSpeed(blockState.getBlock(), serverLevel, blockPos);
                if (randomSource.nextInt((int) (25.f / f) + 1) == 0) {
                    serverLevel.setBlock(blockPos, blockState.setValue(AGES[Math.max(0, config.maxAge-1)], i + 1), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private int getAge(BlockState state) {
        return state.getValue(AGES[Math.max(0, config.maxAge-1)]);
    }

    public void growCrops(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.getAge(blockState) + this.getBonemealAgeIncrease(level);
        int j = this.config.maxAge-1;
        if (i > j) {
            i = j;
        }

        level.setBlock(blockPos, blockState.setValue(AGES[Math.max(0, config.maxAge-1)], i), Block.UPDATE_CLIENTS);
    }

    public BlockState incrementedAge(BlockState blockState) {
        var prop = AGES[Math.max(0, config.maxAge-1)];
        return blockState.setValue(prop, blockState.getValue(prop));
    }

    public boolean isMaxAge(BlockState blockState) {
        var prop = AGES[Math.max(0, config.maxAge-1)];
        return blockState.getValue(prop) == config.maxAge-1;
    }

    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 2, 5);
    }

    protected float getGrowthSpeed(Block block, BlockGetter blockGetter, BlockPos blockPos) {
        float bonus = 1.0F;
        BlockPos blockPos2 = blockPos.below();

        for(int i = -config.bonusRadius; i <= config.bonusRadius; ++i) {
            for(int j = -config.bonusRadius; j <= config.bonusRadius; ++j) {
                float localBonus = 0.f;
                BlockState blockState = blockGetter.getBlockState(blockPos2.offset(i, 0, j));
                if (blockState.is(config.bonusBlock)) {
                    localBonus = 1.f;
                    if (blockState.hasProperty(FarmBlock.MOISTURE) && blockState.getValue(FarmBlock.MOISTURE) > 0) {
                        localBonus = 3.f;
                    }
                }

                if (i != 0 || j != 0) {
                    localBonus /= 4.f;
                }

                bonus += localBonus;
            }
        }

        BlockPos northed = blockPos.north();
        BlockPos southed = blockPos.south();
        BlockPos wested = blockPos.west();
        BlockPos easted = blockPos.east();
        boolean bl = isCrop(blockGetter.getBlockState(wested).getBlock()) || isCrop(blockGetter.getBlockState(easted).getBlock());
        boolean bl2 = isCrop(blockGetter.getBlockState(northed).getBlock()) || isCrop(blockGetter.getBlockState(southed).getBlock());
        if (bl && bl2) {
            bonus /= 2.0F;
        } else {
            boolean bl3 = isCrop(blockGetter.getBlockState(wested.north()).getBlock()) || isCrop(blockGetter.getBlockState(easted.north()).getBlock()) || isCrop(blockGetter.getBlockState(easted.south()).getBlock()) || isCrop(blockGetter.getBlockState(wested.south()).getBlock());
            if (bl3) {
                bonus /= 2.0F;
            }
        }

        return bonus;
    }

    private boolean isCrop(Block block1) {
        return block1.isFilamentBlock() && block1.has(Behaviours.CROP) && Objects.requireNonNull(block1.get(Behaviours.CROP)).config == config;
    }

    protected boolean hasSufficientLight(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getRawBrightness(blockPos, 0) >= config.minLightLevel;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return this.getAge(blockState) < config.maxAge-1;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        if (blockState.getBlock().isFilamentBlock() && !(blockState.getBlock().asFilamentBlock().getPolymerBlockState(blockState, PacketContext.create()).getBlock() instanceof BonemealableBlock)) {
            BlockUtil.handleBoneMealEffects(serverLevel, blockPos);
        }

        this.growCrops(serverLevel, blockPos, blockState);
    }

    public static class Config {
        public int maxAge = 4;
        public int minLightLevel = 8;

        public int bonusRadius = 1;
        public Block bonusBlock = Blocks.FARMLAND;

        public boolean villagerInteraction = true;
        public boolean beeInteraction = true;
    }
}
