package de.tomalbrc.filament.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ExecuteUtil {
    public static void asPlayer(ServerPlayer user, Vec3 at, String ...cmd) {
        var commandSourceStack = user.getServer().createCommandSourceStack().withEntity(user).withMaximumPermission(4);
        if (at != null) {
            commandSourceStack = commandSourceStack.withPosition(at);
        }

        for (int i = 0; i < cmd.length; i++) {
            cmd[i] = cmd[i].replace("${player}", user.getScoreboardName());
            cmd[i] = cmd[i].replace("%player%", user.getScoreboardName());
        }

        as(user.getServer(), commandSourceStack, at, cmd);
    }

    public static void asConsole(ServerPlayer user, Vec3 at, String ... cmd) {
        var commandSourceStack = user.getServer().createCommandSourceStack().withEntity(user).withPosition(user.position()).withRotation(user.getRotationVector()).withMaximumPermission(4);
        if (at != null) {
            commandSourceStack = commandSourceStack.withPosition(at);
        }

        as(user.getServer(), commandSourceStack, at, cmd);
    }

    public static void as(MinecraftServer server, CommandSourceStack commandSourceStack, Vec3 at, String ... cmds) {
        if (at != null) {
            commandSourceStack = commandSourceStack.withPosition(at);
        }

        for (String cmd : cmds) {
            server.getCommands().performPrefixedCommand(commandSourceStack, cmd);
        }
    }
}
