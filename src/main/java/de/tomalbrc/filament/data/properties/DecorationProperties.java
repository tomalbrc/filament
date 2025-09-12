package de.tomalbrc.filament.data.properties;

import de.tomalbrc.filament.decoration.block.DecorationBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.joml.Vector3f;

public class DecorationProperties extends BlockProperties {
    public static final DecorationProperties EMPTY = new DecorationProperties();

    public boolean rotate = false;
    public boolean rotateSmooth = false;

    public Placement placement = Placement.DEFAULT;
    public boolean glow = false;

    public boolean waterloggable = true;

    public ItemDisplayContext display = ItemDisplayContext.FIXED;

    public boolean useItemParticles = true;

    public boolean showBreakParticles = true;

    public boolean drops = true;
    public Vector3f scale;

    @Override
    public BlockBehaviour.Properties toBlockProperties() {
        BlockBehaviour.Properties props = super.toBlockProperties();
        props.lightLevel((state) -> state.hasProperty(DecorationBlock.LIGHT_LEVEL) ? state.getValue(DecorationBlock.LIGHT_LEVEL) : 0);
        return props.dynamicShape().noOcclusion();
    }

    public record Placement(boolean wall, boolean floor, boolean ceiling) {
        public static Placement DEFAULT = new Placement(false, true, false);
        public boolean canPlace(Direction direction) {
            boolean upDown = direction == Direction.UP || direction == Direction.DOWN;
            return (wall && !upDown) || (floor && direction == Direction.UP) || (ceiling && direction == Direction.DOWN);
        }
    }
}
