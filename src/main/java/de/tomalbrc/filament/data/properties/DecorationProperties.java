package de.tomalbrc.filament.data.properties;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockBehaviour;


public class DecorationProperties extends BlockProperties {
    public boolean rotate = false;
    public boolean rotateSmooth = false;

    public Placement placement = Placement.DEFAULT;
    public boolean glow = false;

    public boolean waterloggable = true;
    public boolean solid = false;

    public ItemDisplayContext display = ItemDisplayContext.FIXED;

    public boolean useItemParticles = true;

    public boolean showBreakParticles = true;

    @Override
    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = super.toBlockProperties();
        return props.dynamicShape();
    }

    public record Placement(boolean wall, boolean floor, boolean ceiling) {
        public static Placement DEFAULT = new Placement(false, true, false);
        public boolean canPlace(Direction direction) {
            boolean upDown = direction == Direction.UP || direction == Direction.DOWN;
            return (wall && !upDown) || (floor && direction == Direction.UP) || (ceiling && direction == Direction.DOWN);
        }
    }
}
