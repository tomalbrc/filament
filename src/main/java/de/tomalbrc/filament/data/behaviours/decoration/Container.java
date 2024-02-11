package de.tomalbrc.filament.data.behaviours.decoration;

/**
 * Decoration containers, such as chests, or just drawers etc.
 */
public class Container {
        /**
         * The name displayed in the container UI
         */
        public String name;

        /**
         * The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
         */
        public int size = 9;

        /**
         * Indicates whether the container's contents should be cleared when no player is viewing the inventory.
         */
        public boolean purge = false;

        /**
         * The name of the animation to play when the container is opened (if applicable).
         */
        public String openAnimation = null;

        /**
         * The name of the animation to play when the container is closed (if applicable).
         */
        public String closeAnimation = null;
}
