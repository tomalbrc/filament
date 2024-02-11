package de.tomalbrc.filament.block;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import de.tomalbrc.filament.data.BlockData;

import java.util.HashMap;

public class AxisBlock extends RotatedPillarBlock implements PolymerTexturedBlock {
    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    public AxisBlock(BlockBehaviour.Properties properties, BlockData data) {
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
        return switch (state.getValue(AXIS)) {
            case X -> this.stateMap.get("x");
            case Y -> this.stateMap.get("y");
            case Z -> this.stateMap.get("z");
        };
    }
}
