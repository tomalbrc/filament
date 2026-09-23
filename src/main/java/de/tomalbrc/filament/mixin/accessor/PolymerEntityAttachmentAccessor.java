package de.tomalbrc.filament.mixin.accessor;

import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAttachment.class)
public interface PolymerEntityAttachmentAccessor {
    @Accessor
    Entity getEntity();
}
