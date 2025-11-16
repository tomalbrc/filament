package de.tomalbrc.filament.command;

import com.mojang.brigadier.CommandDispatcher;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.filament.command.subcommand.*;
import de.tomalbrc.filament.util.Constants;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.stream.Collectors;

public class FilamentCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var rootNode = Commands
                .literal(Constants.MOD_ID).requires(Permissions.require("filament.command", 2))
                .executes(ctx -> {
                    var meta = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata();
                    ctx.getSource().sendSuccess(() -> Component.literal(meta.getName() + " " + meta.getVersion().getFriendlyString() + " by " + meta.getAuthors().stream().map(Person::getName).collect(Collectors.joining(", "))), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("Contributors: " + meta.getContributors().stream().map(Person::getName).collect(Collectors.joining(", "))), false);

//                    var itemsLine = String.format("Items: %d (%d combined)", (ItemRegistry.ITEMS_TAGS.size() - DecorationRegistry.REGISTERED_DECORATIONS - BlockRegistry.BLOCKS_TAGS.size()), ItemRegistry.ITEMS_TAGS.size());
//                    var blocksLine = String.format("Blocks: %d (%d)", BlockRegistry.BLOCKS_TAGS.size() - DecorationRegistry.REGISTERED_DECORATIONS, BlockRegistry.BLOCKS_TAGS.size());
//                    for (String s : Arrays.asList(itemsLine, blocksLine, "Decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS, "Decoration block entities: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES)) {
//                        Filament.LOGGER.info(s);
//                        ctx.getSource().sendSuccess(() -> Component.literal(s), false);
//                    }
                    return 0;
                });

        rootNode.then(HatCommand.register());
        rootNode.then(DyeCommand.register());
        rootNode.then(PickCommand.register());
        rootNode.then(ServerItemCommand.register());
        rootNode.then(ClientItemCommand.register());
        rootNode.then(BlockModelTypesCommand.register());

        dispatcher.register(rootNode);
    }
}
