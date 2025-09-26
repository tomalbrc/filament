package de.tomalbrc.filament.behaviour.block;

import de.tomalbrc.filament.api.behaviour.BlockBehaviour;
import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.util.AsyncBlockTicker;
import de.tomalbrc.filament.util.ExecuteUtil;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AreaExecute implements BlockBehaviour<AreaExecute.Config>, AsyncTickingBlockBehaviour {
    public static AsyncBlockTicker.DataKey PLAYERS = new AsyncBlockTicker.DataKey("players");

    private Config config;

    public AreaExecute(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Config getConfig() {
        return this.config;
    }

    @Override
    public void tickAsync(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!config.enabled.getValue(blockState)) return;

        int interval = config.interval.getValue(blockState);
        if (interval <= 0) return; // or treat 0 as "every tick" if that's what you want

        if (serverLevel.getGameTime() % interval != 0) return;

        AABB aABB = (new AABB(blockPos))
                .inflate(config.radius.getValue(blockState))
                .expandTowards(0.0, config.ignoreHeight ? serverLevel.getHeight() : 0, 0.0);

        ReferenceOpenHashSet<ServerPlayer> playersInArea = new ReferenceOpenHashSet<>(
                serverLevel.getEntitiesOfClass(ServerPlayer.class, aABB)
        );

        var tickData = AsyncBlockTicker.get(blockPos);

        @SuppressWarnings("unchecked")
        Set<ServerPlayer> set = (Set<ServerPlayer>) tickData.userData().computeIfAbsent(PLAYERS, x -> ConcurrentHashMap.newKeySet());

        // ENTER + REPEAT
        for (ServerPlayer user : playersInArea) {
            // ENTER: only when not already tracked
            if (!set.contains(user)) {
                var cmds = enterCommands();
                if (cmds != null && (config.permission == null || Permissions.check(user, config.permission))) {
                    var pos = config.atBlock ? blockPos.getCenter() : null;
                    if (getConfig().console) {
                        ExecuteUtil.asConsole(user, pos, cmds.toArray(new String[0]));
                    } else {
                        ExecuteUtil.asPlayer(user, pos, cmds.toArray(new String[0]));
                    }
                }
                // mark inside after enter
                set.add(user);
            }

            var repeat = repeatCommands();
            if (repeat != null && (config.permission == null || Permissions.check(user, config.permission))) {
                var pos = config.atBlock ? blockPos.getCenter() : null;
                if (getConfig().console) {
                    ExecuteUtil.asConsole(user, pos, repeat.toArray(new String[0]));
                } else {
                    ExecuteUtil.asPlayer(user, pos, repeat.toArray(new String[0]));
                }
            }
        }

        // EXIT: if player no longer in playersInArea -> exit
        for (ServerPlayer user : new ReferenceOpenHashSet<>(set)) {
            if (!playersInArea.contains(user) || user.hasDisconnected() || user.isDeadOrDying()) {
                var cmds = exitCommands();
                if (cmds != null && (config.permission == null || Permissions.check(user, config.permission))) {
                    var pos = config.atBlock ? blockPos.getCenter() : null;
                    if (getConfig().console) {
                        ExecuteUtil.asConsole(user, pos, cmds.toArray(new String[0]));
                    } else {
                        ExecuteUtil.asPlayer(user, pos, cmds.toArray(new String[0]));
                    }
                }
                set.remove(user);
            }
        }
    }

    private List<String> enterCommands() {
        return config.enterCommands == null ? this.config.enterCommand == null ? null : List.of(this.config.enterCommand) : config.enterCommands;
    }

    private List<String> exitCommands() {
        return config.exitCommands == null ? this.config.exitCommand == null ? null : List.of(this.config.exitCommand) : config.exitCommands;
    }

    private List<String> repeatCommands() {
        return config.repeatCommands == null ? this.config.repeatCommand == null ? null : List.of(this.config.repeatCommand) : config.repeatCommands;
    }

    public static class Config {
        public BlockStateMappedProperty<Integer> radius = BlockStateMappedProperty.of(16);
        public BlockStateMappedProperty<Boolean> enabled = BlockStateMappedProperty.of(true);
        public BlockStateMappedProperty<Integer> interval = BlockStateMappedProperty.of(40);
        public String repeatCommand;
        public List<String> repeatCommands;
        public String enterCommand;
        public List<String> enterCommands;
        public String exitCommand;
        public List<String> exitCommands;
        public boolean atBlock;
        public boolean ignoreHeight;
        public boolean console;
        public String permission;
    }
}