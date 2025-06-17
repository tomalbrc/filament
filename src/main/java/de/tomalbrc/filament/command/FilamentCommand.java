package de.tomalbrc.filament.command;

import com.mojang.brigadier.CommandDispatcher;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.command.subcommand.*;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import de.tomalbrc.filament.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FilamentCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var rootNode = Commands
                .literal("filament").requires(Permissions.require("filament.command", 2))
                .executes(ctx -> {
                    var meta = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata();
                    ctx.getSource().sendSuccess(() -> Component.literal(meta.getName() + " " + meta.getVersion().getFriendlyString() + " by " + meta.getAuthors().stream().map(Person::getName).collect(Collectors.joining())), false);
                    for (String s : Arrays.asList("Items: " + ItemRegistry.ITEMS_TAGS.size(), "Blocks: " + BlockRegistry.BLOCKS_TAGS.size(), "Decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS, "Decoration block entities: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES)) {
                        Filament.LOGGER.info(s);
                        ctx.getSource().sendSuccess(() -> Component.literal(s), false);
                    }
                    return 0;
                });

        rootNode.then(HatCommand.register());
        rootNode.then(DyeCommand.register());
        rootNode.then(PickCommand.register());
        rootNode.then(ServerItemCommand.register());
        rootNode.then(ClientItemCommand.register());

        dispatcher.register(rootNode);
    }
}
