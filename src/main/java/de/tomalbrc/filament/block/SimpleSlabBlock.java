package de.tomalbrc.filament.block;

import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class SimpleSlabBlock extends SlabBlock implements PolymerTexturedBlock {
    private final Map<BlockState, BlockData.BlockStateMeta> stateMap;
    private final BlockState breakEventState;

    public SimpleSlabBlock(Properties properties, BlockData data) {
        super(properties.dynamicShape());
        this.stateMap = data.createStandardStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.stateMap.get(state).blockState();
    }
}
