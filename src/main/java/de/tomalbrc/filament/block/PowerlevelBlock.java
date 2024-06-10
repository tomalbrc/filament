package de.tomalbrc.filament.block;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import de.tomalbrc.filament.data.BlockData;

import java.util.HashMap;

public class PowerlevelBlock extends SimpleBlock implements PolymerTexturedBlock {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    public PowerlevelBlock(Properties properties, BlockData data) {
        super(properties, data);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));

        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        int signal = blockPlaceContext.getLevel().getBestNeighborSignal(blockPlaceContext.getClickedPos());
        return this.defaultBlockState().setValue(POWER, signal);
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            int power = blockState.getValue(POWER);
            int signal = level.getBestNeighborSignal(blockPos);
            if (signal > this.stateMap.size()-1)
                signal = this.stateMap.size()-1;

            if (signal != power) {
                level.scheduleTick(blockPos, this, 1);
            }
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int signal = serverLevel.getBestNeighborSignal(blockPos);
        if (signal > this.stateMap.size()-1)
            signal = this.stateMap.size()-1;

        if (signal != blockState.getValue(POWER)) {
            serverLevel.setBlock(blockPos, blockState.setValue(POWER, signal), 2); // what does the 2 do..?
        }
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        int power = state.getValue(POWER);
        if (power > this.stateMap.size()-1)
            power = this.stateMap.size()-1;

        return this.stateMap.get(String.valueOf(power));
    }
}
