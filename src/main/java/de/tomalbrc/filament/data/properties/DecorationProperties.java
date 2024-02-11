package de.tomalbrc.filament.data.properties;

import net.minecraft.core.Direction;

public class DecorationProperties extends ItemProperties {
    public boolean rotate = false;
    public boolean rotateSmooth = false;

    public Placement placement = Placement.DEFAULT;
    public boolean glow = false;

    public boolean waterloggable = true;
    public boolean solid = false;

    public record Placement(boolean wall, boolean floor, boolean ceiling) {
        public static Placement DEFAULT = new Placement(false, true, false);
        public boolean canPlace(Direction direction) {
            boolean upDown = direction == Direction.UP || direction == Direction.DOWN;
            return (wall && !upDown) || (floor && direction == Direction.UP) || (ceiling && direction == Direction.DOWN);
        }
    }
}
