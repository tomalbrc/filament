package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Powerlevel implements BlockBehaviour<Powerlevel.PowerlevelConfig> {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0,15);

    private final PowerlevelConfig config;

    public Powerlevel(PowerlevelConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public PowerlevelConfig getConfig() {
        return this.config;
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState block, BlockPlaceContext blockPlaceContext) {
        int signal = blockPlaceContext.getLevel().getBestNeighborSignal(blockPlaceContext.getClickedPos());
        return block.setValue(POWER, Math.min(signal, this.config.max));
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            int power = blockState.getValue(POWER);
            int signal = level.getBestNeighborSignal(blockPos);
            if (signal > this.config.max-1)
                signal = this.config.max-1;

            if (signal != power) {
                level.scheduleTick(blockPos, blockState.getBlock(), 1);
            }
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int signal = serverLevel.getBestNeighborSignal(blockPos);
        if (signal > this.config.max)
            signal = this.config.max;

        if (signal != blockState.getValue(POWER)) {
            serverLevel.setBlock(blockPos, blockState.setValue(POWER, signal), 2); // what does the 2 do..?
        }
    }

    public static class PowerlevelConfig {
        public int max = 15;
    }
}