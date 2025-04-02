package de.tomalbrc.filament.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
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

public class ServerItemCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var node = Commands
                .literal("server-item").requires(Permissions.require("filament.command.server-item", 2));

        return node.executes(ServerItemCommand::execute).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        if (player != null) {
            var handItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, player.registryAccess()), handItem).ifSuccess(tag -> context.getSource().sendSuccess(() -> Component.literal("Server Item: ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)).append(NbtUtils.toPrettyComponent(tag)), false));
            DataComponentMap.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, player.registryAccess()), handItem.getComponents()).ifSuccess(tag -> context.getSource().sendSuccess(() -> Component.literal("Components: ").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)).append(NbtUtils.toPrettyComponent(tag)), false));
        }
        return 0;
    }
}
