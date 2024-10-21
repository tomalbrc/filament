package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class Axis implements BlockBehaviour<Axis.Config> {
    private final Config config;

    public Axis(Config config) {
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
    @NotNull
    public Axis.Config getConfig() {
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

    public static class Config {}
}