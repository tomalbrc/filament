package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import de.tomalbrc.filament.registry.OxidizableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
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
        OxidizableRegistry.add(block, config.replacement);
    }

    @Override
    public WeatherState getAge() {
        return this.config.weatherState;
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return this.getAge() != WeatherState.OXIDIZED;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
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