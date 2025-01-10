package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.bil.api.AnimatedHolder;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Lock behaviour for decoration
 */
public class Lock implements DecorationBehaviour<Lock.LockConfig> {
    public LockConfig lockConfig;
    public boolean unlocked = false;
    public String command = null;

    public Lock(LockConfig lockConfig) {
        this.lockConfig = lockConfig;
    }

    @Override
    @NotNull
    public LockConfig getConfig() {
        return this.lockConfig;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (this.unlocked) return InteractionResult.PASS;

        Item key = this.lockConfig.key == null ? null : BuiltInRegistries.ITEM.get(this.lockConfig.key);
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasHandItem = !mainHandItem.isEmpty();
        boolean holdsKeyAndIsValid = hasHandItem && key != null && mainHandItem.is(key);
        boolean noItemNoKey = !hasHandItem && (key == null);
        if (holdsKeyAndIsValid || noItemNoKey) {
            if (this.lockConfig.consumeKey && hasHandItem) {
                mainHandItem.shrink(1);
            }

            if (this.lockConfig.unlockAnimation != null && !lockConfig.unlockAnimation.isEmpty() && decorationBlockEntity.getDecorationHolder() instanceof AnimatedHolder animatedHolder) {
                animatedHolder.getAnimator().playAnimation(lockConfig.unlockAnimation);
            }

            this.unlocked = !noItemNoKey;

            boolean validCommand = this.command != null && !this.command.isEmpty();
            boolean validLockCommand = this.lockConfig.command != null && !this.lockConfig.command.isEmpty();
            if ((validCommand || validLockCommand) && player.getServer() != null) {
                player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withMaximumPermission(4), validCommand ? this.command : this.lockConfig.command);
            }

            if (this.lockConfig.discard) {
                decorationBlockEntity.destroyStructure(false);
            }
        }

        return this.unlocked ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void read(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        if (compoundTag.contains("Lock")) {
            CompoundTag lock = compoundTag.getCompound("Lock");

            if (compoundTag.contains("Command")) this.command = lock.getString("Command");

            if (compoundTag.contains("Unlocked")) this.unlocked = lock.getBoolean("Unlocked");
        }
    }

    @Override
    public void write(CompoundTag compoundTag, HolderLookup.Provider provider, DecorationBlockEntity blockEntity) {
        CompoundTag lockTag = new CompoundTag();

        if (this.command != null && !this.command.isEmpty()) lockTag.putString("Command", this.command);

        lockTag.putBoolean("Unlocked", this.unlocked);

        compoundTag.put("Lock", lockTag);
    }

    public static class LockConfig {

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
    }
}