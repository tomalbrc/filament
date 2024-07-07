package de.tomalbrc.filament.block;

import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class SimpleSlabBlock extends SlabBlock implements PolymerTexturedBlock {
    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    public SimpleSlabBlock(Properties properties, BlockData data) {
        super(properties.dynamicShape());
        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        var newState = switch (state.getValue(TYPE)) {
            case TOP -> this.stateMap.get("top");
            case BOTTOM -> this.stateMap.get("bottom");
            case DOUBLE -> this.stateMap.get("double");
        };
        return newState.setValue(SlabBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.getPolymerBlockState(state).getBlock();
    }
}
