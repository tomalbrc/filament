package de.tomalbrc.filament.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import de.tomalbrc.filament.util.HideFlags;
import de.tomalbrc.filament.util.Util;

import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class DyeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var dyeNode = Commands
                .literal("dye");

        var colorArg = Commands.argument("color", StringArgumentType
                .string());

        dispatcher.register(dyeNode.then(colorArg.executes(DyeCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() != null) {
            ItemStack item = context.getSource().getPlayer().getMainHandItem();
            if (item != null && !item.isEmpty()) {
                String hexColorString = getString(context, "color");
                Optional<Integer> color = Util.validateAndConvertHexColor(hexColorString);
                if (color.isPresent()) {
                    CompoundTag itemNbt = item.getOrCreateTag();
                    CompoundTag displayTag = new CompoundTag();
                    displayTag.putInt("color", color.get());
                    itemNbt.put("display", displayTag);
                    itemNbt.putInt("HideFlags", HideFlags.HideDyed.getValue());
                    return Command.SINGLE_SUCCESS;
                }
            }
        }
        return 0;
    }
}
