package de.tomalbrc.filament.mixin;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ModelTemplate.class)
public interface ModelTemplateAccessor {
    @Accessor
    default Set<TextureSlot> getRequiredSlots() {
        throw new UnsupportedOperationException();
    }
}
