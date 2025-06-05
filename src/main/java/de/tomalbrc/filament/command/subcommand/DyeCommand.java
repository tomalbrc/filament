package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.filament.util.Util;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class DyeCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var dyeNode = Commands
                .literal("dye").requires(Permissions.require("filament.command.dye", 2));

        var colorArg = Commands.argument("color", StringArgumentType
                .string());

        return dyeNode.then(colorArg.executes(DyeCommand::execute)).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() != null) {
            ItemStack item = context.getSource().getPlayer().getMainHandItem();
            if (!item.isEmpty()) {
                String hexColorString = getString(context, "color");
                Optional<Integer> color = Util.validateAndConvertHexColor(hexColorString);
                if (color.isPresent()) {
                    item.set(DataComponents.DYED_COLOR, new DyedItemColor(color.get()));
                    return Command.SINGLE_SUCCESS;
                }
            }
        }
        return 0;
    }
}
