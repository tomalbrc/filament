package de.tomalbrc.filament.decoration.util.impl;

import de.tomalbrc.filament.data.behaviours.decoration.Lock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;

public class LockImpl {
    public Lock lock;
    public boolean unlocked = false;
    public String command = null;

    public LockImpl(Lock lock) {
        this.lock = lock;
    }

    public boolean interact(ServerPlayer player, DecorationBlockEntity decorationBlockEntity) {
        if (this.unlocked)
            return false;

        Item key = this.lock.key == null ? null : BuiltInRegistries.ITEM.get(this.lock.key);
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasHandItem = !mainHandItem.isEmpty();
        boolean holdsKeyAndIsValid = hasHandItem && key != null && mainHandItem.is(key);
        boolean noItemNoKey = !hasHandItem && (key == null);
        if (holdsKeyAndIsValid || noItemNoKey) {
            if (this.lock.consumeKey && hasHandItem) {
                mainHandItem.shrink(1);
            }

            // TODO: FIX LOCK ANIMATION
            //if (this.lock.unlockAnimation != null && !lock.unlockAnimation.isEmpty() && decorationBlockEntity instanceof AjDecorationBlockEntity ajDecorationEntity) {
            //    decorationBlockEntity.play(lock.unlockAnimation);
            //}

            this.unlocked = true;

            boolean validCommand = this.command != null && !this.command.isEmpty();
            boolean validLockCommand = this.lock.command != null && !this.lock.command.isEmpty();
            if ((validCommand || validLockCommand) && player.getServer() != null) {
                player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack().withPosition(decorationBlockEntity.getBlockPos().getCenter()), validCommand ? this.command : this.lock.command);
            }

            if (this.lock.discard) {
                decorationBlockEntity.destroyStructure(false);
            }
        }

        return this.unlocked;
    }

    public void read(CompoundTag compoundTag) {
        if (compoundTag.contains("Lock")) {
            CompoundTag lock = compoundTag.getCompound("Lock");

            if (compoundTag.contains("Command"))
                this.command = lock.getString("Command");

            if (compoundTag.contains("Unlocked"))
                this.unlocked = lock.getBoolean("Unlocked");
        }
    }

    public void write(CompoundTag compoundTag) {
        CompoundTag lockTag = new CompoundTag();

        if (this.command != null && !this.command.isEmpty())
            lockTag.putString("Command", this.command);

        lockTag.putBoolean("Unlocked", this.unlocked);

        compoundTag.put("Lock", lockTag);
    }
}
