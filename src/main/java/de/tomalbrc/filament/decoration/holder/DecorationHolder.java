package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.world.phys.Vec3;

public class DecorationHolder extends ElementHolder {
    private final DecorationBlockEntity parent;

    public DecorationHolder(DecorationBlockEntity blockEntity) {
        super();
        this.parent = blockEntity;
        DecorationUtil.setup(this, blockEntity);
    }

    @Override
    public <T extends VirtualElement> T addElement(T element) {
        T res = super.addElement(element);
        if (element instanceof InteractionElement interactionElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(interactionElement.getEntityId(), this.parent::getItem);
        }
        if (element instanceof ItemFrameElement itemFrameElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(itemFrameElement.getEntityId(), this.parent::getItem);
        }
        return res;
    }

    @Override
    protected void onAttachmentRemoved(HolderAttachment oldAttachment) {
        for (VirtualElement element : this.getElements()) {
            if (element instanceof InteractionElement interactionElement) {
                DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.remove(interactionElement.getEntityId());
            }
            if (element instanceof ItemFrameElement interactionElement) {
                DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.remove(interactionElement.getEntityId());
            }
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
