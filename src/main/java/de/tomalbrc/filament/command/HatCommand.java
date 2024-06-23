package de.tomalbrc.filament.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.tomalbrc.filament.util.Util;
import net.minecraft.client.main.Main;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class HatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var hatNode = Commands
                .literal("hat");

        dispatcher.register(hatNode.executes(HatCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        if (player != null) {
            var handItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            var headItem = player.getItemBySlot(EquipmentSlot.HEAD);
            player.setItemSlot(EquipmentSlot.HEAD, handItem);
            player.setItemInHand(InteractionHand.MAIN_HAND, headItem);
        }
        return 0;
    }
}
