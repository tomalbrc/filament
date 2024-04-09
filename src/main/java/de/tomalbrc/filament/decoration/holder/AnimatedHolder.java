package de.tomalbrc.filament.decoration.holder;

import de.tomalbrc.bil.core.holder.positioned.PositionedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.data.behaviours.decoration.Animation;
import de.tomalbrc.filament.data.DecorationData;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public class AnimatedHolder extends PositionedHolder {
    private final DecorationBlockEntity decorationBlockEntity;

    public AnimatedHolder(DecorationBlockEntity blockEntity, Model model) {
        super((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos().getCenter(), model);
        this.decorationBlockEntity = blockEntity;

        this.updateCullingBox();

        if (this.decorationBlockEntity.getDecorationData().hasAnimation()) {
            this.setAnimationData(decorationBlockEntity.getDecorationData().behaviour().animation);
        }
    }

    public void setRotation(float rotation) {
        this.getElements().forEach(x -> {
            if (x instanceof DisplayElement displayElement) {
                displayElement.setTeleportDuration(0);
                displayElement.setYaw(rotation);
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

    public void setAnimationData(@NotNull Animation animationData) {
        if (animationData.model != null) {
            if (model == null) {
                Filament.LOGGER.error("No AnimatedJava model named '" + animationData.model + "' was found!");
            } else {
                if (animationData.autoplay != null) {
                    this.getAnimator().playAnimation(animationData.autoplay);
                }

                this.getElements().forEach(x -> {
                    if (x instanceof ItemDisplayElement itemDisplayElement) {
                        itemDisplayElement.setYaw(this.decorationBlockEntity.getVisualRotationYInDegrees());
                    }
                });

                DecorationData decorationData = decorationBlockEntity.getDecorationData();
                if (this.decorationBlockEntity.containerImpl != null && decorationData != null && decorationData.isContainer()) {
                    if (Objects.requireNonNull(decorationData.behaviour()).container.openAnimation != null) {
                        this.decorationBlockEntity.containerImpl.container().setOpenCallback(() -> {
                            assert this.decorationBlockEntity.animatedHolder != null;
                            this.decorationBlockEntity.animatedHolder.getAnimator().playAnimation(decorationData.behaviour().container.openAnimation, 2);
                        });
                    }
                    if (Objects.requireNonNull(decorationData.behaviour()).container.closeAnimation != null) {
                        this.decorationBlockEntity.containerImpl.container().setCloseCallback(() -> {
                            assert this.decorationBlockEntity.animatedHolder != null;
                            this.decorationBlockEntity.animatedHolder.getAnimator().playAnimation(decorationData.behaviour().container.closeAnimation, 2);
                        });
                    }
                }
            }
        }
    }
}