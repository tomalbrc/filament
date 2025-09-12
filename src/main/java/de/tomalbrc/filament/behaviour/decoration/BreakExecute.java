package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lock behaviour for decoration
 */
public class BreakExecute implements DecorationBehaviour<BreakExecute.Config> {
    public Config config;

    public BreakExecute(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public void postBreak(DecorationBlockEntity decorationBlockEntity, BlockPos blockPos, Player player) {
        var commands = commands();
        boolean hasCommands = commands != null;
        if (hasCommands && player.getServer() != null && player instanceof ServerPlayer serverPlayer) {
            var pos = config.atBlock ? blockPos.getCenter() : null;
            if (getConfig().console) {
                ExecuteUtil.asConsole(serverPlayer, pos, commands.toArray(new String[0]));
            }
            else {
                ExecuteUtil.asPlayer(serverPlayer, pos, commands.toArray(new String[0]));
            }
        }
    }

    private List<String> commands() {
        return getConfig().commands == null ? this.getConfig().command == null ? null : List.of(this.getConfig().command) : getConfig().commands;
    }

    public static class Config {
        public String command = null;
        public List<String> commands = null;
        public boolean atBlock = false;
        public boolean console;
    }
}