package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExecuteAttackBlock implements BlockBehaviour<ExecuteAttackBlock.Config> {
    private final Config config;

    public ExecuteAttackBlock(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ExecuteAttackBlock.Config getConfig() {
        return config;
    }

    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) runCommandBlock(serverPlayer, blockPos);
    }

    public void runCommandBlock(ServerPlayer user, BlockPos blockPos) {
        var cmds = commands();
        if (cmds != null) {
            if (config.console) {
                ExecuteUtil.asConsole(user, config.atBlock ? blockPos.getCenter() :  null, cmds.toArray(new String[0]));
            }else {
                ExecuteUtil.asPlayer(user, config.atBlock ? blockPos.getCenter() :  null, cmds.toArray(new String[0]));
            }

            if (this.config.sound != null) {
                var sound = this.config.sound;
                user.level().playSound(null, user, BuiltInRegistries.SOUND_EVENT.getValue(sound), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            if (this.config.consumes) {
                user.level().destroyBlock(blockPos, config.dropBlock, user);
            }
        }
    }

    private List<String> commands() {
        return config.commands == null ? this.config.command == null ? null : List.of(this.config.command) : config.commands;
    }

    public static class Config {
        public boolean consumes;

        public String command;
        public List<String> commands;

        public boolean atBlock = false;

        public boolean console = false;

        public boolean dropBlock = false;

        public Identifier sound;
    }
}
