package de.tomalbrc.filament.behaviour.block;

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
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.NotNull;

public class Powerlevel implements BlockBehaviour<Powerlevel.PowerlevelConfig> {
    public static final IntegerProperty[] POWERS = {
            IntegerProperty.create("powerlevel", 0,1),
            IntegerProperty.create("powerlevel", 0,2),
            IntegerProperty.create("powerlevel", 0,3),
            IntegerProperty.create("powerlevel", 0,4),
            IntegerProperty.create("powerlevel", 0,5),
            IntegerProperty.create("powerlevel", 0,6),
            IntegerProperty.create("powerlevel", 0,7),
            IntegerProperty.create("powerlevel", 0,8),
            IntegerProperty.create("powerlevel", 0,9),
            IntegerProperty.create("powerlevel", 0,10),
            IntegerProperty.create("powerlevel", 0,11),
            IntegerProperty.create("powerlevel", 0,12),
            IntegerProperty.create("powerlevel", 0,13),
            IntegerProperty.create("powerlevel", 0,14),
            IntegerProperty.create("powerlevel", 0,15),
    };

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
        builder.add(POWERS[Math.max(0, config.max-1)]);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState block, BlockPlaceContext blockPlaceContext) {
        int signal = blockPlaceContext.getLevel().getBestNeighborSignal(blockPlaceContext.getClickedPos());
        return block.setValue(POWERS[Math.max(0, config.max-1)], Math.min(signal, this.config.max));
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, Orientation orientation, boolean bl) {
        if (!level.isClientSide) {
            int power = blockState.getValue(POWERS[Math.max(0, config.max-1)]);
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

        if (signal != blockState.getValue(POWERS[Math.max(0, config.max-1)])) {
            serverLevel.setBlock(blockPos, blockState.setValue(POWERS[Math.max(0, config.max-1)], signal), 2); // what does the 2 do..?
        }
    }

    public static class PowerlevelConfig {
        public int max = 15;
    }
}