package de.tomalbrc.filament.data.properties;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

public class DecorationProperties extends ItemProperties {
    public boolean rotate = false;
    public boolean rotateSmooth = false;

    public Placement placement = Placement.DEFAULT;
    public boolean glow = false;

    public PushReaction pushReaction = PushReaction.NORMAL;

    public boolean waterloggable = true;
    public boolean solid = false;

    public ItemDisplayContext display = ItemDisplayContext.FIXED;

    @NotNull
    public Block blockBase = Blocks.STONE;

    public boolean useItemParticles = true;

    public boolean showBreakParticles = true;

    public record Placement(boolean wall, boolean floor, boolean ceiling) {
        public static Placement DEFAULT = new Placement(false, true, false);
        public boolean canPlace(Direction direction) {
            boolean upDown = direction == Direction.UP || direction == Direction.DOWN;
            return (wall && !upDown) || (floor && direction == Direction.UP) || (ceiling && direction == Direction.DOWN);
        }
    }
}
