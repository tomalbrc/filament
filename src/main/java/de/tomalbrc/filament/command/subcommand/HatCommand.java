package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class HatCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var hatNode = Commands
                .literal("hat").requires(Permissions.require("filament.command.hat", 2));

        return hatNode.executes(HatCommand::execute).build();
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
