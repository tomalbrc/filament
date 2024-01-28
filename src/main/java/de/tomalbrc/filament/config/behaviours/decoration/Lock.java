package de.tomalbrc.filament.config.behaviours.decoration;

import net.minecraft.resources.ResourceLocation;

/**
 * Lock behaviours for decoration
 */
public class Lock {
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
         * `data modify @e[entitySpecifier] Lock.Command set value "say hello"`
         */
        public String command = null;
}