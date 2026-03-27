package de.tomalbrc.filamentweb;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.filamentweb.service.AuthFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.net.URI;
import java.util.UUID;

public class EditorCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        var hatNode = Commands
                .literal("editor").requires(Permissions.require("filament.editor", PermissionLevel.ADMINS.id()));

        return hatNode.executes(EditorCommand::execute).build();
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            key.append((int) (Math.random() * 100) % 10);
        }

        if (player != null) {
            var uri = FilamentEditorConfig.getInstance().externalAddress + "/login?id=" + player.getStringUUID();
            context.getSource().sendSystemMessage(
                    Component.literal("Open Editor: " + uri)
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(uri)))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(uri)))
                            ).append(Component.literal(", your key: " + key.toString()).withStyle(Style.EMPTY))
            );

            AuthFilter.REQUESTS.put(player.getUUID(), key.toString());
        } else {
            var id = UUID.randomUUID();
            AuthFilter.SERVER_REQUEST.put(id, key.toString());

            var uri = FilamentEditorConfig.getInstance().externalAddress + "/login?id=" + id;
            context.getSource().sendSystemMessage(
                    Component.literal("Open Editor: " + uri)
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(uri)))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(uri)))
                            ).append(Component.literal(", your key: " + key.toString()).withStyle(Style.EMPTY))
            );
        }
        return 0;
    }
}
