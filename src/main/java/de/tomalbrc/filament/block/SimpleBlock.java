package de.tomalbrc.filament.block;

import de.tomalbrc.filament.config.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class SimpleBlock extends Block implements PolymerTexturedBlock {
    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    private final BlockData blockData;

    public SimpleBlock(BlockBehaviour.Properties properties, BlockData data) {
        super(properties);
        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
        this.blockData = data;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.getPolymerBlockState(state).getBlock();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.stateMap.get("default");
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }


    @Override
    public boolean isSignalSource(BlockState blockState) {
        return this.blockData.isPowersource();
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (this.blockData.isPowersource()) {
            assert this.blockData.behaviour() != null;
            return this.blockData.behaviour().powersource.value;
        } else {
            return 0;
        }
    }
}
