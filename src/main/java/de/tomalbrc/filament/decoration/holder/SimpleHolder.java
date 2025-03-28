package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.util.DecorationUtil;
import de.tomalbrc.filament.util.Util;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleHolder extends ElementHolder {
    private ItemDisplayElement displayElement;

    public SimpleHolder() {
        super();
    }

    @Override
    protected void updateInitialPosition() {
        super.updateInitialPosition();

        if (this.getAttachment() instanceof ChunkAttachment chunkAttachment) {
            this.setBlock(BlockPos.containing(chunkAttachment.getPos()), chunkAttachment.getChunk().getBlockState(BlockPos.containing(chunkAttachment.getPos())));
        }
    }

    public void setBlock(BlockPos pos, BlockState blockState) {
        if (DecorationRegistry.isDecoration(blockState) && this.displayElement == null) {
            SimpleDecorationBlock decorationBlock = (SimpleDecorationBlock) blockState.getBlock();

            this.displayElement = DecorationUtil.decorationItemDisplay(decorationBlock.getDecorationData(), Direction.UP, Util.SEGMENTED_ANGLE8.toDegrees(blockState.getValue(SimpleDecorationBlock.ROTATION)));
            this.addElement(displayElement);

            if (!decorationBlock.getDecorationData().hasBlocks()) {
                InteractionElement interactionElement = DecorationUtil.decorationInteraction(decorationBlock.getDecorationData());
                this.addElement(interactionElement);
            }
        }
    }
}