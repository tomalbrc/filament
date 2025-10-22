package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.decoration.block.ComplexDecorationBlock;
import de.tomalbrc.filament.decoration.util.DecorationItemDisplayElement;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DecorationHolder extends ElementHolder implements FilamentDecorationHolder {
    private final Supplier<ItemStack> pickResult;

    public DecorationHolder(Supplier<ItemStack> pickResult) {
        super();
        this.pickResult = pickResult;
    }

    @Override
    @SuppressWarnings("all")
    protected void onAttachmentSet(HolderAttachment attachment, @Nullable HolderAttachment oldAttachment) {
        BlockBoundAttachment boundAttachment;
        if (attachment != null && (boundAttachment = (BlockBoundAttachment) attachment).getBlockState().getBlock() instanceof ComplexDecorationBlock) {
            Filament.SERVER.schedule(new TickTask(0, () -> {
                boundAttachment.setBlockState(boundAttachment.getBlockState());
            }));
        }
    }

    @Override
    public void updateVisualItem(ItemStack newItem) {
        for (VirtualElement element : this.getElements()) {
            if (element instanceof DecorationItemDisplayElement itemDisplayElement) {
                itemDisplayElement.setItem(newItem);
            }
        }

        this.tick();
    }

    @Override
    public void playAnimation(ServerPlayer serverPlayer, String animation, int priority, Consumer<ServerPlayer> onFinish) {
        if (onFinish != null)
            onFinish.accept(serverPlayer);
    }

    @Override
    public <T extends VirtualElement> T addElement(T element) {
        T res = super.addElement(element);
        if (element instanceof InteractionElement interactionElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(interactionElement.getEntityId(), this::getPickResult);
        }
        if (element instanceof ItemFrameElement itemFrameElement) {
            DecorationUtil.VIRTUAL_ENTITY_PICK_MAP.put(itemFrameElement.getEntityId(), this::getPickResult);
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

        super.onAttachmentRemoved(oldAttachment);
    }

    @Override
    public void notifyUpdate(HolderAttachment.UpdateType updateType) {
        super.notifyUpdate(updateType);

        if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && getAttachment() != null) {
            this.update(((BlockAwareAttachment)this.getAttachment()).getBlockState());
        }
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return this.pickResult.get();
    }
}