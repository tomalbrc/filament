package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.AnimatedChest;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.registry.OxidizableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for oxidizing blocks (like copper)
 * Copies blockstate properties if applicable
 */
public class Oxidizable implements BlockBehaviour<Oxidizable.Config>, WeatheringCopper {
    private final Config config;

    public Oxidizable(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Oxidizable.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(Item item, Block block, BehaviourHolder behaviourHolder) {
        if (config.replacement != null)
            OxidizableRegistry.add(block, config.replacement);
    }

    @Override
    public @NotNull WeatherState getAge() {
        return this.config.weatherState;
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return this.getAge() != WeatherState.OXIDIZED && config.replacement != null;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        var rightSideChest = blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT;
        if (blockState.hasProperty(ChestBlock.TYPE)) {
            DecorationBlockEntity decorationBlockEntity = (DecorationBlockEntity) serverLevel.getBlockEntity(blockPos);
            if (decorationBlockEntity != null) {
                AnimatedChest animatedChest = decorationBlockEntity.get(Behaviours.ANIMATED_CHEST);
                if (animatedChest != null && animatedChest.container.hasViewers()) return;
            }
        }

        if (!rightSideChest && (!blockState.hasProperty(DoorBlock.HALF) || blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)) {
            this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
        }
    }

    public static class Config {
        /**
         * Replacement block
         */
        public ResourceLocation replacement;

        /**
         * Weather state, can be `unaffected`, `exposed`, `weathered`, `oxidized`
         */
        public WeatherState weatherState = WeatherState.UNAFFECTED;
    }
}