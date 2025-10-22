package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class DyeCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var dyeNode = Commands
                .literal("dye").requires(Permissions.require("filament.command.dye", 2));

        var colorArg = Commands.argument("color", HexColorArgument.hexColor());

        return dyeNode.then(colorArg.executes(DyeCommand::execute)).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() != null) {
            ItemStack item = context.getSource().getPlayer().getMainHandItem();
            if (!item.isEmpty()) {
                Integer hexColor = HexColorArgument.getHexColor(context, "color");
                item.set(DataComponents.DYED_COLOR, new DyedItemColor(hexColor));
                return Command.SINGLE_SUCCESS;
            }
        }
        return 0;
    }
}
