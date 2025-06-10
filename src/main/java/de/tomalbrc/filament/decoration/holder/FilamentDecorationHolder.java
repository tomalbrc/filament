package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.filament.decoration.block.SimpleDecorationBlock;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    default void playAnimation(String animation, int priority) {
    }

    default void playAnimation(String animation) {
    }

    default void setYaw(float rotation) {
        for (VirtualElement element : this.asPolymerHolder().getElements()) {
            if (element instanceof GenericEntityElement displayElement) {
                displayElement.setYaw(rotation);
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
        if (attachment != null) {
            BlockEntity blockEntity = attachment.getWorld().getBlockEntity(BlockPos.containing(this.getAttachment().getPos()));
            if (blockEntity == null) {
                this.setYaw(blockState.getValue(SimpleDecorationBlock.ROTATION) * 45 - 180);
                this.tick();
            }
        }
    }
}
