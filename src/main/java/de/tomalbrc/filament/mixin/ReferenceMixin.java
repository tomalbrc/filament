package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;

// for ability to supply tags in filament jsons
@Mixin(Holder.Reference.class)
public abstract class ReferenceMixin<T> {
    @Shadow public abstract boolean isBound();

    @Shadow @Nullable private ResourceKey<T> key;

    @Shadow @Nullable private Set<TagKey<T>> tags;

    @Inject(method = "bindTags", at = @At("HEAD"), cancellable = true)
    private void dd(Collection<TagKey<T>> collection, CallbackInfo ci) {
        System.out.println("1 "+key.registryKey().location());
        System.out.println("2 "+BuiltInRegistries.ITEM.key().location());
        if (this.key != null && this.key.registryKey().location() == BuiltInRegistries.ITEM.key().location() && ItemRegistry.ITEMS_TAGS.containsKey(this.key.location())) {
            // inject tags
            Set<TagKey<T>> modifiableCopy = new ObjectArraySet<>(collection);
            var tags = ItemRegistry.ITEMS_TAGS.get(this.key.location());
            if (tags != null) for (ResourceLocation tag : tags) {
                TagKey<T> newTagKey = TagKey.create(key.registryKey(), tag);
                modifiableCopy.add(newTagKey);
            }

            this.tags = modifiableCopy;
            ci.cancel();

        } else if (this.key != null && this.key.registryKey().location() == BuiltInRegistries.BLOCK.key().registry() && BlockRegistry.BLOCKS_TAGS.containsKey(this.key.location())) {
            // inject tags
            Set<TagKey<T>> modifiableCopy = new ObjectArraySet<>(collection);
            var tags = BlockRegistry.BLOCKS_TAGS.get(this.key.location());
            if (tags != null) for (ResourceLocation tag : tags) {
                TagKey<T> newTagKey = TagKey.create(key.registryKey(), tag);
                modifiableCopy.add(newTagKey);
            }
            this.tags = modifiableCopy;
            ci.cancel();
        }
    }
}
