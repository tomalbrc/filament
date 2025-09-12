package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.ExecuteUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lock behaviour for decoration
 */
public class Lock implements DecorationBehaviour<Lock.Config> {
    public Config config;
    public boolean unlocked = false;

    public Lock(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Lock.Config getConfig() {
        return this.config;
    }

    @Override
    public void init(DecorationBlockEntity blockEntity) {
        DecorationBehaviour.super.init(blockEntity);
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (this.unlocked && !config.repeatable) return InteractionResult.PASS;

        Item key = this.config.key == null ? null : BuiltInRegistries.ITEM.getValue(this.config.key);
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasHandItem = !mainHandItem.isEmpty();
        boolean holdsKeyAndIsValid = hasHandItem && key != null && mainHandItem.is(key);
        boolean noItemNoKey = !hasHandItem && (key == null);
        if (holdsKeyAndIsValid || noItemNoKey) {
            if (this.config.consumeKey && hasHandItem) {
                mainHandItem.shrink(1);
            }

            if (this.config.unlockAnimation != null && !config.unlockAnimation.isEmpty() && decorationBlockEntity.getOrCreateHolder() != null) {
                decorationBlockEntity.getOrCreateHolder().playAnimation(config.unlockAnimation);
            }

            this.unlocked = !noItemNoKey;

            var cmds = commands();
            boolean hasCommand = cmds != null && !cmds.isEmpty();
            if (hasCommand && player.getServer() != null) {
                var pos = getConfig().atBlock ? decorationBlockEntity.getBlockPos().getCenter() : null;
                if (getConfig().console) {
                    ExecuteUtil.asConsole(player, pos, cmds.toArray(new String[0]));
                }
                else {
                    ExecuteUtil.asPlayer(player, pos, cmds.toArray(new String[0]));
                }
            }

            if (this.config.discard) {
                decorationBlockEntity.destroyStructure(false);
            }
        }

        return this.unlocked ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void read(ValueInput input, DecorationBlockEntity blockEntity) {
        input.child("Lock").ifPresent(lock -> this.unlocked = lock.getBooleanOr("Unlocked", this.unlocked));
    }

    @Override
    public void write(ValueOutput output, DecorationBlockEntity blockEntity) {
        ValueOutput lockTag = output.child("Lock");
        lockTag.putBoolean("Unlocked", this.unlocked);
    }

    private List<String> commands() {
        return getConfig().commands == null ? this.getConfig().command == null ? null : List.of(this.getConfig().command) : getConfig().commands;
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
        public String unlockAnimation = null;

        /**
         * Command to execute when the lock is successfully unlocked (if specified).
         * The command can be overwritten using NBT, the path for the command is Lock.Command in the block entities' NBT
         * `formats modify @e[entitySpecifier] Lock.Command set value "say hello"`
         */
        public String command = null;
        public List<String> commands = null;

        public boolean atBlock = false;

        public boolean repeatable = true;
        public boolean console;
    }
}