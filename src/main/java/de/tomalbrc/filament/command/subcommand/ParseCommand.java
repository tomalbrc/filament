package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.filament.util.FilamentFormatter;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import static net.minecraft.commands.Commands.argument;

public class ParseCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var node = Commands
                .literal("parse").requires(Permissions.require("filament.command.parse", 2)).then(argument("text", StringArgumentType.greedyString()));

        return node.executes(ParseCommand::execute).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        var text = StringArgumentType.getString(context, "text");
        if (player != null && text != null) {
            context.getSource().sendSuccess(() -> FilamentFormatter.parse(text), false);
        }
        return 0;
    }
}
