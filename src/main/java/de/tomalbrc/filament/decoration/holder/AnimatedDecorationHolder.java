package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.bil.core.holder.base.SimpleAnimatedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Animation;
import de.tomalbrc.filament.behaviour.decoration.Container;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.util.ItemFrameElement;
import de.tomalbrc.filament.util.DecorationUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AnimatedDecorationHolder extends SimpleAnimatedHolder implements FilamentDecorationHolder {
    private final DecorationBlockEntity parent;

    public AnimatedDecorationHolder(DecorationBlockEntity blockEntity, Model model) {
        super(model);
        this.parent = blockEntity;

        if (this.parent.has(Behaviours.ANIMATION)) {
            Animation.AnimationConfig animation = this.parent.getDecorationData(). behaviour().get(Behaviours.ANIMATION);
            this.setAnimationData(animation);
        }

        DecorationUtil.setupElements(this, blockEntity.getDecorationData(), blockEntity.getDirection(), blockEntity.getVisualRotationYInDegrees(), parent.getItem(), parent::interact);
    }

    @Override
    public void setYaw(float rotation) {
        this.getElements().forEach(x -> {
            if (x instanceof DisplayElement displayElement) {
                displayElement.setTeleportDuration(0);
                displayElement.setYaw(rotation-180);
            }
        });
    }

    @Override
    public void applyPose(ServerPlayer serverPlayer, Pose pose, DisplayWrapper<?> display) {
        super.applyPose(serverPlayer, pose, display);
        display.element().setTranslation(serverPlayer, pose.translation().get(new Vector3f()).sub(0, 0.5f, 0));
    }

    @Override
    protected void onDataLoaded() {
        super.onDataLoaded();
        if (this.bones != null && this.parent.getDecorationData() != null && this.parent.getDecorationData().size() != null) {
            for (Bone<?> bone : this.bones) {
                bone.element().setDisplaySize(this.parent.getDecorationData().size().get(0) * 1.5f, this.parent.getDecorationData().size().get(1) * 1.5f);
            }
        }
    }

    public void setAnimationData(@NotNull Animation.AnimationConfig animationData) {
        if (animationData.model != null) {
            if (model == null) {
                Filament.LOGGER.error("No Animated model named '{}' was found!", animationData.model);
            } else {
                if (animationData.autoplay != null) {
                    this.getAnimator().playAnimation(animationData.autoplay);
                }

                this.setYaw(this.parent.getVisualRotationYInDegrees());

                if (this.parent.has(Behaviours.CONTAINER)) {
                    Container container = this.parent.get(Behaviours.CONTAINER);
                    assert container != null;

                    if (container.getConfig().openAnimation != null) {
                        container.container.setOpenCallback(() -> this.parent.getOrCreateHolder().playAnimation(container.getConfig().openAnimation, 2));
                    }
                    if (container.getConfig().closeAnimation != null) {
                        container.container.setCloseCallback(() -> this.parent.getOrCreateHolder().playAnimation(container.getConfig().closeAnimation, 2));
                    }
                }
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
    public void playAnimation(ServerPlayer serverPlayer, String animation, int priority) {
        this.getAnimator().playAnimation(serverPlayer, animation, priority);
    }

    @Override
    public void playAnimation(ServerPlayer serverPlayer, String animation) {
        this.getAnimator().playAnimation(serverPlayer, animation);
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