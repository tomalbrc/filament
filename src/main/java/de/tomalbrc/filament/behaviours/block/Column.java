package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

/**
 * Block behaviourConfig for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Column implements BlockBehaviour<Column.ColumnConfig> {
    private final ColumnConfig config;

    public Column(ColumnConfig config) {
        this.config = config;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return RotatedPillarBlock.rotatePillar(blockState, rotation);
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(AXIS, Direction.Axis.Y);
    }

    @Override
    public ColumnConfig getConfig() {
        return this.config;
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState self, BlockPlaceContext blockPlaceContext) {
        return self.setValue(AXIS, blockPlaceContext.getClickedFace().getAxis());
    }

    public static class ColumnConfig {}
}