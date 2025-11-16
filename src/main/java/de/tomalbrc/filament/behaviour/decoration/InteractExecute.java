package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interact-execute behaviour for decoration
 */
public class InteractExecute implements DecorationBehaviour<InteractExecute.Config> {
    public Config config;

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
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        Item key = this.config.key == null ? null : BuiltInRegistries.ITEM.get(this.config.key);
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasHandItem = !mainHandItem.isEmpty();
        boolean holdsKeyAndIsValid = hasHandItem && key != null && mainHandItem.is(key);
        boolean noKey = key == null;
        var pos = getConfig().atBlock ? decorationBlockEntity.getBlockPos().getCenter() : null;
        if (holdsKeyAndIsValid || noKey) {
            if (this.config.consumeKey && hasHandItem) {
                mainHandItem.shrink(1);
            } else if (this.config.damageKey) {
                mainHandItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }

            if (this.config.animation != null && !config.animation.isEmpty() && decorationBlockEntity.getOrCreateHolder() != null) {
                // TODO: 1.21.1 per player animation
                decorationBlockEntity.getOrCreateHolder().playAnimation(config.animation, 0, commandsPost() == null ? null : (() -> {
                    var cmdsPost = commandsPost();
                    if (cmdsPost != null) {
                        if (getConfig().console) {
                            ExecuteUtil.asConsole(player, pos, cmdsPost.toArray(new String[0]));
                        }
                        else {
                            ExecuteUtil.asPlayer(player, pos, cmdsPost.toArray(new String[0]));
                        }
                    }

                    if (config.animationPost != null) {
                        // TODO: 1.21.1 per player animation
                        decorationBlockEntity.getOrCreateHolder().playAnimation(config.animationPost);
                    }
                }));
            }

            var cmds = commands();
            boolean hasCommand = cmds != null && !cmds.isEmpty();
            if (hasCommand) {
                if (getConfig().console) {
                    ExecuteUtil.asConsole(player, pos, cmds.toArray(new String[0]));
                }
                else {
                    ExecuteUtil.asPlayer(player, pos, cmds.toArray(new String[0]));
                }

                return InteractionResult.SUCCESS;
            }

            if (this.config.discard) {
                decorationBlockEntity.destroyStructure(false);
            }
        } else if (!noKey) {
            var cmds = commandsIncorrectKey();
            if (cmds != null) {
                if (getConfig().console) {
                    ExecuteUtil.asConsole(player, pos, cmds.toArray(new String[0]));
                }
                else {
                    ExecuteUtil.asPlayer(player, pos, cmds.toArray(new String[0]));
                }
            }

            if (config.animationIncorrectKey != null) {
                // TODO: 1.21.1 per player animation
                decorationBlockEntity.getOrCreateHolder().playAnimation(config.animationIncorrectKey);
            }
        }

        return InteractionResult.CONSUME;
    }

    private List<String> commands() {
        return config.commands == null ? config.command == null ? null : List.of(config.command) : config.commands;
    }

    private List<String> commandsPost() {
        return config.commandsPostAnimation == null ? config.commandPostAnimation == null ? null : List.of(config.commandPostAnimation) : config.commandsPostAnimation;
    }

    private List<String> commandsIncorrectKey() {
        return config.commandsIncorrectKey == null ? config.commandIncorrectKey == null ? null : List.of(config.commandIncorrectKey) : config.commandsIncorrectKey;
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
         * Damage the key item
         */
        public boolean damageKey = false;

        /**
         * Specifies whether the lock util should be discarded after unlocking.
         */
        public boolean discard = false;

        /**
         * Name of the animation to play upon successful unlocking (if applicable).
         */
        public String animation = null;

        /**
         * Name of animation to player after the first one ended
         */
        public String animationPost = null;

        /**
         * Whether to play animation only for player interacting with the decoration
         */
        public boolean animatePerPlayer = false;

        /**
         * Command to execute when the lock is successfully unlocked (if specified).
         * The command can be overwritten using NBT, the path for the command is Lock.Command in the block entities' NBT
         * `data modify @e[...] Lock.Command set value "say hello"`
         */
        public String command = null;

        /**
         * List of commands. See above
         */
        public List<String> commands = null;

        /**
         * Commands to run after animation
         */
        public String commandPostAnimation = null;

        /**
         * List of to run after animation
         */
        public List<String> commandsPostAnimation = null;

        /**
         * List of commands to run on incorrect key
         */
        public String commandIncorrectKey = null;
        public List<String> commandsIncorrectKey = null;
        public String animationIncorrectKey = null;

        /**
         * Whether to execute the commands at the interacted blocks' position instead of the players position
         */
        public boolean atBlock = false;
        public boolean console;
    }
}