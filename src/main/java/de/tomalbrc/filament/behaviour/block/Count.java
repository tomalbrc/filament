package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Count implements BlockBehaviour<Count.CountConfig> {
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1,16);

    private final CountConfig config;

    public Count(CountConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public CountConfig getConfig() {
        return this.config;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }

    @Override
    public Optional<Boolean> canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return Optional.of(!blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().getItem() == blockState.getBlock().asItem() && blockState.getValue(COUNT) < config.max);
    }

    @Override
    public BlockState getStateForPlacement(BlockState self, BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(self.getBlock())) {
            BlockState newState = blockState.cycle(COUNT);
            if (newState.getValue(COUNT) > this.config.max)
                return newState.setValue(COUNT, this.config.max);
            else
                return newState;
        }

        return self;
    }

    public static class CountConfig {
        public int max = 4;
    }
}
