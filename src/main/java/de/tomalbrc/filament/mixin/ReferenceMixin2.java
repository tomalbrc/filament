package de.tomalbrc.filament.mixin;

import de.tomalbrc.filament.registry.ItemRegistry;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mixin(Holder.Reference.class)
public abstract class ReferenceMixin2<T> implements Holder<T> {
    @Shadow @Nullable private T value;

    @Shadow @Nullable private Set<TagKey<T>> tags;

    @Inject(method = "bindTags", at = @At("HEAD"), cancellable = true)
    private void filament$bind(Collection<TagKey<T>> collection, CallbackInfo ci) {
        Item x = ItemRegistry.COPY_TAGS.get(value);
        if (x != null) {
            Map<TagKey<Item>, HolderSet.Named<Item>> set = ((MappedRegistryAccessor)BuiltInRegistries.ITEM).getTags();
            var newSet = new ReferenceArraySet<TagKey<T>>();
            set.forEach((tagKey,blockNamed) -> {
                for (Holder<Item> holder : blockNamed) {
                    if (holder == x) {
                        newSet.add((TagKey<T>) tagKey);
                    }
                }
            });

            this.tags = Set.copyOf(newSet);
            ci.cancel();
        }
    }
}
