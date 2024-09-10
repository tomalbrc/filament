package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.AnimatedHolder;
import de.tomalbrc.filament.registry.ModelRegistry;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Animation behaviour for decoration using animated java models (powered by nylon)
 */
public class Animation implements DecorationBehaviour<Animation.AnimationConfig> {
    private final AnimationConfig config;

    public Animation(AnimationConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public AnimationConfig getConfig() {
        return this.config;
    }

    @Override
    public ElementHolder createHolder(DecorationBlockEntity blockEntity) {
        Model model = ModelRegistry.getModel(config.model);
        if (model == null) {
            Filament.LOGGER.error("No Animated model named '" + config.model + "' was found!");
        } else {
            return new AnimatedHolder(blockEntity, model);
        }

        return null;
    }

    @Override
    public void onElementAttach(DecorationBlockEntity blockEntity, ElementHolder holder) {
        if (holder instanceof AnimatedHolder animatedHolder) {
            animatedHolder.setRotation(blockEntity.getVisualRotationYInDegrees());
        }
    }

    public static class AnimationConfig {
        /**
         * The name of the animated model associated with this animation (if applicable).
         */
        public ResourceLocation model = null;

        /**
         * The name of the animation to autoplay (if specified)
         */
        public String autoplay = null;
    }
}