package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.world.phys.Vec3;

public class DecorationHolder extends ElementHolder {
    private final DecorationBlockEntity parent;

    public DecorationHolder(DecorationBlockEntity blockEntity) {
        super();
        this.parent = blockEntity;
        this.setup(blockEntity);
    }

    private void setup(DecorationBlockEntity blockEntity) {
        if (blockEntity.getDecorationData().hasBlocks()) {
            this.addElement(DecorationUtil.decorationItemDisplay(this.parent));
        } else if (blockEntity.getDecorationData().size() != null) {
            this.addElement(DecorationUtil.decorationItemDisplay(this.parent));
            this.addElement(DecorationUtil.decorationInteraction(this.parent));
        } else {
            // TODO: Why do virtual Item frames show item names?!
            //ItemFrameElement itemFrameElement = new ItemFrameElement(this.parent);
            //this.addElement(itemFrameElement);

            // Just using display+interaction again
            this.addElement(DecorationUtil.decorationItemDisplay(this.parent));
            this.addElement(DecorationUtil.decorationInteraction(this.parent));
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    public Vec3 getPos() {
        return this.getAttachment() != null ? this.getAttachment().getPos() : null;
    }
}
