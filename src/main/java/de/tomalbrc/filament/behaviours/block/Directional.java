package de.tomalbrc.filament.behaviours.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

/**
 * Block behaviourConfig for strippable blocks (with an axe)
 * Copies blockstate properties if applicabable
 */
public class Directional implements BlockBehaviour<Directional.DirectionalConfig> {
    private final DirectionalConfig config;

    public Directional(DirectionalConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public DirectionalConfig getConfig() {
        return this.config;
    }

    @Override
    public BlockState modifyDefaultState(BlockState blockState) {
        return blockState.setValue(BlockStateProperties.FACING, Direction.SOUTH);
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState selfDefault, BlockPlaceContext blockPlaceContext) {
        return selfDefault.setValue(BlockStateProperties.FACING, blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.FACING, rotation.rotate(blockState.getValue(BlockStateProperties.FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.FACING)));
    }

    public static class DirectionalConfig {}
}