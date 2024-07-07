package de.tomalbrc.filament.block;

import com.mojang.serialization.MapCodec;
import de.tomalbrc.filament.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class DirectionBlock extends DirectionalBlock implements PolymerTexturedBlock {
    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    public MapCodec<DirectionBlock> codec() {
        return null;
    }

    public DirectionBlock(Properties properties, BlockData data) {
        super(properties);
        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.getPolymerBlockState(state).getBlock();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayer player) {
        return this.breakEventState;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return stateMap.get(state.getValue(FACING).getSerializedName());
    }
}
