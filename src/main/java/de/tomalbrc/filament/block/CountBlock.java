package de.tomalbrc.filament.block;

import de.tomalbrc.filament.config.data.BlockData;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.HashMap;

public class CountBlock extends SimpleBlock implements PolymerTexturedBlock {
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 4);

    private final HashMap<String, BlockState> stateMap;
    private final BlockState breakEventState;

    public CountBlock(Properties properties, BlockData data) {
        super(properties, data);
        this.registerDefaultState(this.stateDefinition.any().setValue(COUNT, 1));

        this.stateMap = data.createStateMap();
        this.breakEventState = data.properties().blockBase.defaultBlockState();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return !blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().getItem() == this.asItem() && blockState.getValue(COUNT) < 4 || super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return blockState.cycle(COUNT);
        }
        return super.getStateForPlacement(blockPlaceContext);
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
        return switch (state.getValue(COUNT)) {
            case 2 -> this.stateMap.get("2");
            case 3 -> this.stateMap.get("3");
            case 4 -> this.stateMap.get("4");
            default -> this.stateMap.get("1");
        };
    }
}
