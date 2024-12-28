package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.DecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * For interactive lamps
 */
public class Lamp implements DecorationBehaviour<Lamp.Config> {

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
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        var blockState = decorationBlockEntity.getBlockState();
        var lightLevel = blockState.getValue(DecorationBlock.LIGHT_LEVEL);
        var level = decorationBlockEntity.getLevel();
        if (config.cycle != null && !config.cycle.isEmpty() && level != null) {
            var currentIndex = config.cycle.indexOf(lightLevel);
            var next = config.cycle.get((currentIndex+1) % config.cycle.size());
            level.setBlockAndUpdate(decorationBlockEntity.getBlockPos(), blockState.setValue(DecorationBlock.LIGHT_LEVEL, next));
        } else if (level != null) {
            if (lightLevel == config.on) {
                level.setBlockAndUpdate(decorationBlockEntity.getBlockPos(), blockState.setValue(DecorationBlock.LIGHT_LEVEL, config.off));
            } else {
                level.setBlockAndUpdate(decorationBlockEntity.getBlockPos(), blockState.setValue(DecorationBlock.LIGHT_LEVEL, config.on));
            }
        }

        return InteractionResult.PASS;
    }

    public static class Config {
        int on = 15;
        int off = 0;
        List<Integer> cycle;
    }
}