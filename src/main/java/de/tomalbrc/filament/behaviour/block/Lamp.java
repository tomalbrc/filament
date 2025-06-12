package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * For interactive lamps
 */
public class Lamp implements BlockBehaviour<Lamp.Config> {

    private final Config config;

    public Lamp(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Lamp.Config getConfig() {
        return this.config;
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        blockState.setValue(BlockUtil.LIGHT_LEVEL, config.cycle != null && !config.cycle.isEmpty() ? config.cycle.getFirst() : config.defaultValue == null ? config.off : config.defaultValue);
        return BlockBehaviour.super.getStateForPlacement(blockState, blockPlaceContext);
    }

    @Override
    public boolean createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockUtil.LIGHT_LEVEL);
        return true;
    }

    @Override
    public net.minecraft.world.level.block.state.BlockBehaviour.Properties modifyBlockProperties(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        props.lightLevel((state) -> state.hasProperty(BlockUtil.LIGHT_LEVEL) ? state.getValue(BlockUtil.LIGHT_LEVEL) : 0);
        return props;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        var lightLevel = blockState.getValue(BlockUtil.LIGHT_LEVEL);
        if (config.cycle != null && !config.cycle.isEmpty() && level != null) {
            var currentIndex = config.cycle.indexOf(lightLevel);
            var next = config.cycle.get((currentIndex+1) % config.cycle.size());
            level.setBlockAndUpdate(blockPos, blockState.setValue(BlockUtil.LIGHT_LEVEL, next));
            return InteractionResult.CONSUME;
        } else if (level != null) {
            if (lightLevel == config.on) {
                level.setBlockAndUpdate(blockPos, blockState.setValue(BlockUtil.LIGHT_LEVEL, config.off));
            } else {
                level.setBlockAndUpdate(blockPos, blockState.setValue(BlockUtil.LIGHT_LEVEL, config.on));
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public static class Config {
        int on = 15;
        int off = 0;
        Integer defaultValue = 0;
        List<Integer> cycle;
    }
}