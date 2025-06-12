package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.api.behaviour.DecorationRotationProvider;
import de.tomalbrc.filament.util.BlockUtil;
import de.tomalbrc.filament.util.Util;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class Rotating implements BlockBehaviour<Rotating.Config>, DecorationRotationProvider {
    private final Config config;

    public Rotating(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Rotating.Config getConfig() {
        return this.config;
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockUtil.ROTATION);
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockState selfDefault, BlockPlaceContext blockPlaceContext) {
        if (config.smooth) {
            return selfDefault.setValue(BlockUtil.ROTATION, Util.SEGMENTED_ANGLE8.fromDegrees(blockPlaceContext.getRotation()));
        } else {
            return selfDefault.setValue(BlockUtil.ROTATION, Util.SEGMENTED_ANGLE8.fromDirection(blockPlaceContext.getHorizontalDirection()));
        }
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockUtil.ROTATION, rotation.rotate(state.getValue(BlockUtil.ROTATION), 8));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(BlockUtil.ROTATION, mirror.mirror(state.getValue(BlockUtil.ROTATION), 8));
    }

    @Override
    public float getVisualRotationYInDegrees(BlockState blockState) {
        return Util.SEGMENTED_ANGLE8.toDegrees(blockState.getValue(BlockUtil.ROTATION));
    }

    public static class Config {
        public boolean smooth;
    }
}