package de.tomalbrc.filament.mixin;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
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
