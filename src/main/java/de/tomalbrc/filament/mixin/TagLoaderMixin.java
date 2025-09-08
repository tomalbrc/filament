package de.tomalbrc.filament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.EntityRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

// for ability to supply tags in filament jsons
@Mixin(TagLoader.class)
public abstract class TagLoaderMixin {

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"), cancellable = true)
    private void filament$customTags(ResourceManager resourceManager, CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir, @Local(ordinal = 0) ResourceLocation id, @Local(ordinal = 1) ResourceLocation id2, @Local List<TagLoader.EntryWithSource> list) {
        if (id.getPath().startsWith("tags/item")) {
            var collection = ItemRegistry.ITEMS_TAGS.get(id2);
            if (collection != null) for (ResourceLocation itemId : collection) {
                list.add(new TagLoader.EntryWithSource(TagEntry.element(itemId), itemId.getNamespace()));
            }
        }
        else if (id.getPath().startsWith("tags/block")) {
            var collection = BlockRegistry.BLOCKS_TAGS.get(id2);
            if (collection != null) for (ResourceLocation itemId : collection) {
                list.add(new TagLoader.EntryWithSource(TagEntry.element(itemId), itemId.getNamespace()));
            }
        }
        else if (id.getPath().startsWith("tags/entity")) {
            var collection = EntityRegistry.ENTITY_TAGS.get(id2);
            if (collection != null) for (ResourceLocation itemId : collection) {
                list.add(new TagLoader.EntryWithSource(TagEntry.element(itemId), itemId.getNamespace()));
            }
        }
    }
}
