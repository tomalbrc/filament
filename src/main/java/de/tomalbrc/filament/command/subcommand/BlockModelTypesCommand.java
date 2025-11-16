package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BlockModelTypesCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var node = Commands
                .literal("block-model-types").requires(Permissions.require("filament.command.block-model-types", 2));

        return node.executes(BlockModelTypesCommand::execute).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        if (player != null) {
            var r = Arrays.stream(BlockModelType.values()).map(x -> {
                var blocksLeft = PolymerBlockResourceUtils.getBlocksLeft(x);
                return String.format("§3%s§r: §" + (blocksLeft <= 3 ? "4" : blocksLeft <= 10 ? "6" : "2") + "%d§r", x.name().toLowerCase(), blocksLeft);
            }).collect(Collectors.joining(", "));
            context.getSource().sendSuccess(() -> Component.literal("BlockModelTypes: " + r), false);
        }
        return 0;
    }
}
