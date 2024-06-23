package de.tomalbrc.filament.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class HatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var hatNode = Commands
                .literal("hat").requires(Permissions.require("filament.command.hat", 1));

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
