package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class HorizontalFacing implements BlockBehaviour<HorizontalFacing.Config>, DecorationRotationProvider {
    private final Config config;

    public HorizontalFacing(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public HorizontalFacing.Config getConfig() {
        return this.config;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockState selfDefault, BlockPlaceContext blockPlaceContext) {
        return selfDefault.setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public float getVisualRotationYInDegrees(BlockState blockState) {
        return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite().toYRot();
    }

    public static class Config {}
}