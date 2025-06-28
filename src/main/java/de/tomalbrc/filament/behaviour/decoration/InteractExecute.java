package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.bil.util.command.CommandParser;
import de.tomalbrc.bil.util.command.ParsedCommand;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interact-execute behaviour for decoration
 */
public class InteractExecute implements DecorationBehaviour<InteractExecute.Config> {
    public Config config;
    public ParsedCommand[] parsedCommands = null;
    public ParsedCommand[] parsedCommandsPost = null;

    public InteractExecute(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public InteractExecute.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        DecorationBehaviour.super.init(blockEntity);

        var commands = commands();
        if (commands != null) {
            List<ParsedCommand> commandList = new ArrayList<>();
            for (String command : commands) {
                commandList.addAll(Arrays.asList(CommandParser.parse(command)));
            }
            this.parsedCommands = commandList.toArray(new ParsedCommand[0]);
        }

        var commandsPost = commandsPost();
        if (commandsPost != null) {
            List<ParsedCommand> commandListPost = new ArrayList<>();
            for (String command : commandsPost) {
                commandListPost.addAll(Arrays.asList(CommandParser.parse(command)));
            }
            this.parsedCommandsPost = commandListPost.toArray(new ParsedCommand[0]);
        }
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        Item key = this.config.key == null ? null : BuiltInRegistries.ITEM.getValue(this.config.key);
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasHandItem = !mainHandItem.isEmpty();
        boolean holdsKeyAndIsValid = hasHandItem && key != null && mainHandItem.is(key);
        boolean noItemNoKey = !hasHandItem && (key == null);
        if (holdsKeyAndIsValid || noItemNoKey) {
            if (this.config.consumeKey && hasHandItem) {
                mainHandItem.shrink(1);
            }

            if (this.config.animation != null && !config.animation.isEmpty() && decorationBlockEntity.getOrCreateHolder() != null) {
                decorationBlockEntity.getOrCreateHolder().playAnimation(config.animatePerPlayer ? player : null, config.animation, 0, commandsPost() == null ? null : (serverPlayer -> {
                    var css = player.createCommandSourceStack().withSource(player.getServer()).withMaximumPermission(4);
                    if (getConfig().atBlock)
                        css = css.withPosition(decorationBlockEntity.getBlockPos().getCenter());

                    if (player.getServer() != null && parsedCommandsPost != null) {
                        for (ParsedCommand cmd : parsedCommandsPost) {
                            cmd.execute(player.getServer().getCommands().getDispatcher(), css);
                        }
                    }
                }));
            }

            boolean hasCommand = parsedCommands != null && parsedCommands.length > 0;
            boolean validLockCommand = this.config.command != null && !this.config.command.isEmpty();
            if ((hasCommand || validLockCommand) && player.getServer() != null) {
                var css = player.createCommandSourceStack().withSource(player.getServer()).withMaximumPermission(4);
                if (getConfig().atBlock)
                    css = css.withPosition(decorationBlockEntity.getBlockPos().getCenter());

                if (hasCommand) {
                    for (ParsedCommand cmd : parsedCommands) {
                        cmd.execute(player.getServer().getCommands().getDispatcher(), css);
                    }
                }

                return InteractionResult.CONSUME;
            }

            if (this.config.discard) {
                decorationBlockEntity.destroyStructure(false);
            }
        }

        return InteractionResult.PASS;
    }

    private List<String> commands() {
        return config.commands == null ? config.command == null ? null : List.of(config.command) : config.commands;
    }

    private List<String> commandsPost() {
        return config.commandsPostAnimation == null ? config.commandPostAnimation == null ? null : List.of(config.commandPostAnimation) : config.commandsPostAnimation;
    }

    public static class Config {

        /**
         * The identifier of the key required to unlock.
         */
        public ResourceLocation key = null;

        /**
         * Determines whether the key should be consumed upon unlocking.
         */
        public boolean consumeKey = false;

        /**
         * Specifies whether the lock util should be discarded after unlocking.
         */
        public boolean discard = false;

        /**
         * Name of the animation to play upon successful unlocking (if applicable).
         */
        public String animation = null;

        /**
         * Whether to play animation only for player interacting with the decoration
         */
        public boolean animatePerPlayer = false;

        /**
         * Command to execute when the lock is successfully unlocked (if specified).
         * The command can be overwritten using NBT, the path for the command is Lock.Command in the block entities' NBT
         * `formats modify @e[entitySpecifier] Lock.Command set value "say hello"`
         */
        public String command = null;

        /**
         * List of commands. See above
         */
        public List<String> commands = null;

        /**
         * Whether to run commands after animation
         */
        public String commandPostAnimation = null;
        public List<String> commandsPostAnimation = null;

        public boolean atBlock = false;
    }
}