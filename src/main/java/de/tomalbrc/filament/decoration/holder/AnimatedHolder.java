package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.bil.core.holder.positioned.PositionedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.behaviour.decoration.Animation;
import de.tomalbrc.filament.behaviour.decoration.Container;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AnimatedHolder extends PositionedHolder {
    private final DecorationBlockEntity decorationBlockEntity;

    public AnimatedHolder(DecorationBlockEntity blockEntity, Model model) {
        super((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos().getCenter(), model);
        this.decorationBlockEntity = blockEntity;

        this.updateCullingBox();

        if (this.decorationBlockEntity.has(Behaviours.ANIMATION)) {
            Animation.AnimationConfig animation = this.decorationBlockEntity.getDecorationData().behaviourConfig().get(Behaviours.ANIMATION);
            this.setAnimationData(animation);
        }
    }

    public void setRotation(float rotation) {
        this.getElements().forEach(x -> {
            if (x instanceof DisplayElement displayElement) {
                displayElement.setTeleportDuration(0);
                displayElement.setYaw(rotation - 180);
            }
        });
    }

    @Override
    public void applyPose(Pose pose, DisplayWrapper display) {
        super.applyPose(pose, display);
        display.element().setTranslation(pose.translation().get(new Vector3f()).sub(0, 0.5f, 0));
    }

    protected void updateCullingBox() {
        if (this.decorationBlockEntity.getDecorationData() != null && this.decorationBlockEntity.getDecorationData().size() != null) {
            for (Bone bone : this.bones) {
                bone.element().setDisplaySize(this.decorationBlockEntity.getDecorationData().size().get(0) * 1.5f, this.decorationBlockEntity.getDecorationData().size().get(1) * 1.5f);
            }
        }
    }

    public void setAnimationData(@NotNull Animation.AnimationConfig animationData) {
        if (animationData.model != null) {
            if (model == null) {
                Filament.LOGGER.error("No Animated model named '" + animationData.model + "' was found!");
            } else {
                if (animationData.autoplay != null) {
                    this.getAnimator().playAnimation(animationData.autoplay);
                }

                this.getElements().forEach(x -> {
                    if (x instanceof ItemDisplayElement itemDisplayElement) {
                        itemDisplayElement.setYaw(this.decorationBlockEntity.getVisualRotationYInDegrees());
                    }
                });

                if (this.decorationBlockEntity.has(Behaviours.CONTAINER)) {
                    Container container = this.decorationBlockEntity.get(Behaviours.CONTAINER);

                    if (container.getConfig().openAnimation != null) {
                        container.container.setOpenCallback(() -> {
                            if (this.decorationBlockEntity.getDecorationHolder() instanceof AnimatedHolder animatedHolder)
                                animatedHolder.getAnimator().playAnimation(container.getConfig().openAnimation, 2);
                        });
                    }
                    if (container.getConfig().closeAnimation != null) {
                        container.container.setCloseCallback(() -> {
                            if (this.decorationBlockEntity.getDecorationHolder() instanceof AnimatedHolder animatedHolder)
                                animatedHolder.getAnimator().playAnimation(container.getConfig().closeAnimation, 2);
                        });
                    }
                }
            }
        }
    }
}