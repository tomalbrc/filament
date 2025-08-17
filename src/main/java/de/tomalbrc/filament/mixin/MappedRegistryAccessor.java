package de.tomalbrc.filament.mixin;

import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {
    @Accessor
    Map<TagKey<T>, HolderSet.Named<T>> getTags();
}
