package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleDecorationHolder extends ElementHolder {
    private ItemDisplayElement displayElement;

    public SimpleDecorationHolder() {
        super();
    }

    @Override
    protected void updateInitialPosition() {
        super.updateInitialPosition();

        if (this.getAttachment() instanceof ChunkAttachment chunkAttachment) {
            this.setBlock(chunkAttachment.getChunk().getBlockState(BlockPos.containing(chunkAttachment.getPos())));
        }
    }

    public void setBlock(BlockState blockState) {
        if (DecorationRegistry.isDecoration(blockState) && this.displayElement == null) {
            SimpleDecorationBlock decorationBlock = (SimpleDecorationBlock) blockState.getBlock();
            DecorationData data = decorationBlock.getDecorationData();

            this.displayElement = DecorationUtil.decorationItemDisplay(data, Direction.UP, Util.SEGMENTED_ANGLE8.toDegrees(blockState.getValue(SimpleDecorationBlock.ROTATION)));
            this.addElement(displayElement);

            if (!data.hasBlocks()) {
                InteractionElement interactionElement = DecorationUtil.decorationInteraction(data);
                this.addElement(interactionElement);
            }
        }
    }

    @Override
    public <T extends VirtualElement> T addElement(T element) {
        if (element instanceof InteractionElement interactionElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(interactionElement.getEntityId(), this.displayElement::getItem);
        }
        if (element instanceof ItemFrameElement itemFrameElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(itemFrameElement.getEntityId(), this.displayElement::getItem);
        }
        return super.addElement(element);
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
}