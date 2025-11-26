package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.data.properties.BlockStateMappedProperty;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.holder.AnimatedDecorationHolder;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import de.tomalbrc.filament.registry.ModelRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;

/**fi
 * Animation behaviour for decoration using animated java models
 */
public class Animation implements DecorationBehaviour<Animation.Config> {
    private final Config config;

    public Animation(Config config) {
        this.config = config;
    }

    @Override
    @NotNull
    public Animation.Config getConfig() {
        return this.config;
    }

    @Override
    public FilamentDecorationHolder createHolder(DecorationBlockEntity blockEntity) {
        Model model = ModelRegistry.getModel(config.model);
        if (model == null) {
            Filament.LOGGER.error("No Animated model named '{}' was found!", config.model);
        } else {
            return new AnimatedDecorationHolder(blockEntity, model);
        }

        return null;
    }

    @Override
    public void onHolderAttach(DecorationBlockEntity blockEntity, FilamentDecorationHolder holder) {
        holder.setYaw(blockEntity.getVisualRotationYInDegrees());
    }

    @Override
    public void read(ValueInput output, DecorationBlockEntity blockEntity) {
        output.getString("Animation").ifPresent(x -> blockEntity.getOrCreateHolder().playAnimation(x));

        DecorationBehaviour.super.read(output, blockEntity);
    }

    public static class Config {
        /**
         * The name of the animated model associated with this animation (if applicable).
         */
        public ResourceLocation model = null;

        /**
         * The name of the animation to autoplay (if specified)
         */
        public String autoplay = null;

        public BlockStateMappedProperty<String> variant = BlockStateMappedProperty.of("default");
    }
}