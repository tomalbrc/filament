package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lock behaviour for decoration
 */
public class BreakExecute implements DecorationBehaviour<BreakExecute.Config> {
    public Config config;
    public String command = null;

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
        if (hasCommands && player.getServer() != null) {
            var css = player.createCommandSourceStack().withSource(player.getServer()).withMaximumPermission(4);
            if (getConfig().atBlock)
                css = css.withPosition(decorationBlockEntity.getBlockPos().getCenter());

            for (String cmd : commands) {
                player.getServer().getCommands().performPrefixedCommand(css, cmd);
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
    }
}