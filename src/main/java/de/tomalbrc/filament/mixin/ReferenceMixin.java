package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.mixin.accessor.MappedRegistryAccessor;
import de.tomalbrc.filament.registry.ItemRegistry;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;

@Mixin(Holder.Reference.class)
public abstract class ReferenceMixin<T> implements Holder<T> {
    @Shadow @Nullable private T value;

    @Shadow @Nullable private Set<TagKey<T>> tags;

    @Inject(method = "bindTags", at = @At("HEAD"), cancellable = true)
    private void filament$bindItemTags(Collection<TagKey<T>> collection, CallbackInfo ci) {
        if (ItemRegistry.COPY_TAGS.containsKey(value)) {
            var x = ItemRegistry.COPY_TAGS.get(value);
            if (x != null) {
                var set = ((MappedRegistryAccessor<T>)BuiltInRegistries.ITEM).getTags();
                var newSet = new ReferenceArraySet<TagKey<T>>();
                set.forEach((tagKey,holders) -> {
                    for (Holder<?> holder : holders) {
                        if (holder == x) {
                            newSet.add(tagKey);
                        }
                    }
                });

                this.tags = Set.copyOf(newSet);
                ci.cancel();
            }
        }
    }
}
