package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.bil.core.holder.positioned.PositionedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Frame;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Animation;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.DecorationItemDisplayElement;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class AnimatedDecorationHolder extends PositionedHolder implements FilamentDecorationHolder {
    private final DecorationBlockEntity parent;
    private BlockStateMappedProperty<String> variantProperty;

    public AnimatedDecorationHolder(DecorationBlockEntity blockEntity, Model model) {
        super((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos().getCenter(), model);
        this.parent = blockEntity;

        if (this.parent.has(Behaviours.ANIMATION)) {
            Animation.Config animation = this.parent.getDecorationData().behaviour().get(Behaviours.ANIMATION);
            this.setAnimationData(animation);

            this.variantProperty = animation.variant;
        }

        DecorationUtil.setupElements(this, blockEntity.getDecorationData(), blockEntity.getDirection(), blockEntity.getVisualRotationYInDegrees(), parent.getItem(), parent::interact);
    }

    private void updateVariant(BlockState blockState) {
        if (this.variantProperty != null) {
            if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
            }

            var variantName = this.variantProperty.getValue(blockState);
            if (variantName != null) {
                this.getVariantController().setVariant(variantName);
            }
        }
    }

//    @Override
//    public boolean canRunEffects(ServerPlayer serverPlayer, Frame frame) {
//        BlockState state = parent.getBlockState();
//
//        // TODO: This should be done through decoration behaviours...
//        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
//            return state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.RIGHT;
//        }
//
//        return super.canRunEffects(serverPlayer, frame);
//    }

    @Override
    public void setYaw(float rotation) {
        this.getElements().forEach(x -> {
            if (x instanceof ItemDisplayElement displayElement) {
                displayElement.setTeleportDuration(0);
                displayElement.setYaw(rotation-180);
            }
        });
    }

    @Override
    public void applyPose(Pose pose, DisplayWrapper<?> display) {
        super.applyPose(pose, display);
        display.element().setTranslation(pose.translation().get(new Vector3f()).sub(0, 0.5f, 0));
    }

    @Override
    protected void onDataLoaded() {
        super.onDataLoaded();
        if (this.bones != null && this.parent.getDecorationData() != null && this.parent.getDecorationData().size() != null) {
            for (Bone bone : this.bones) {
                bone.element().setDisplaySize(this.parent.getDecorationData().size().get(0) * 1.5f, this.parent.getDecorationData().size().get(1) * 1.5f);
            }
        }

        updateVariant(this.parent.getBlockState());
    }

    public void setAnimationData(@NotNull Animation.Config animationData) {
        if (animationData.model != null) {
            if (model == null) {
                Filament.LOGGER.error("No Animated model named '{}' was found!", animationData.model);
            } else {
                if (animationData.autoplay != null) {
                    this.getAnimator().playAnimation(animationData.autoplay);
                }

                this.setYaw(this.parent.getVisualRotationYInDegrees());
            }
        }
    }

    @Override
    public void notifyUpdate(HolderAttachment.UpdateType updateType) {
        super.notifyUpdate(updateType);

        if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && getAttachment() != null) {
            this.update(((BlockAwareAttachment)this.getAttachment()).getBlockState());
        }
    }

    @Override
    public void update(BlockState blockState) {
        FilamentDecorationHolder.super.update(blockState);
        updateVariant(blockState);
    }

    @Override
    public void playAnimation(String animation, int priority, Runnable onFinish) {
        this.getModel().animations().keySet().forEach(this.getAnimator()::stopAnimation);
        this.getAnimator().playAnimation(animation, priority, onFinish);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return parent.getItem().copy();
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
}