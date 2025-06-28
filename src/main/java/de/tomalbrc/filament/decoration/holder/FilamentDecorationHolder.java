package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.decoration.block.DecorationBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface FilamentDecorationHolder {
    <T extends VirtualElement> T addElement(T element);

    void removeElement(VirtualElement element);

    void tick();

    Vec3 getPos();

    @Nullable HolderAttachment getAttachment();

    boolean isAnimated();

    ItemStack getPickResult();

    default void updateVisualItem(ItemStack newItem) {

    }

    void playAnimation(ServerPlayer serverPlayer, String animation, int priority, Consumer<ServerPlayer> onFinish);

    default void playAnimation(ServerPlayer serverPlayer, String animation, int priority) {
        playAnimation(null, animation, priority, null);
    }
    default void playAnimation(String animation, int priority) {
        playAnimation(null, animation, priority, null);
    }
    default void playAnimation(ServerPlayer serverPlayer, String animation) {
        playAnimation(serverPlayer, animation, 0, null);
    }
    default void playAnimation(String animation) {
        playAnimation(null, animation, 0, null);
    }

    default void setYaw(float rotation) {
        for (VirtualElement element : this.asPolymerHolder().getElements()) {
            if (element instanceof GenericEntityElement displayElement) {
                displayElement.setYaw(rotation-180);
            }
        }

        if (!isAnimated())
            tick();
    }

    default ElementHolder asPolymerHolder() {
        return (ElementHolder) this;
    }

    default void update(BlockState blockState) {
        HolderAttachment attachment = this.getAttachment();
        if (attachment instanceof BlockAwareAttachment blockBoundAttachment) {
            BlockState attachmentBlockState = blockBoundAttachment.getBlockState();
            DecorationBlock decorationBlock = (DecorationBlock) attachmentBlockState.getBlock();
            // the decoration block entity does not have the blockstate available when update() is called, use blockState from attachment
            this.updateVisualItem(decorationBlock.visualItemStack(blockBoundAttachment.getWorld(), blockBoundAttachment.getBlockPos(), blockState));
            this.setYaw(decorationBlock.getVisualRotationYInDegrees(blockState));
            this.tick();
        }
    }
}
