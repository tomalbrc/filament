package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.behaviour.BehaviourHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Repairable behaviour
 */
public class Repairable implements ItemBehaviour<Repairable.Config> {
    private final Config config;

    List<TagKey<Item>> compiledTags = new ObjectArrayList<>();
    List<ResourceLocation> compiledItems = new ObjectArrayList<>();

    public Repairable(Config config) {
        this.config = config;
    }

    @Override
    public void init(Item item, BehaviourHolder behaviourHolder) {
        for (String string : config.items) {
            if (string.startsWith("#")) {
                var key = string.substring(1);
                this.compiledTags.add(TagKey.create(Registries.ITEM, ResourceLocation.parse(key)));
            } else {
                this.compiledItems.add(ResourceLocation.parse(string));
            }
        }
    }

    @Override
    @NotNull
    public Repairable.Config getConfig() {
        return this.config;
    }

    @Override
    @NotNull
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        for (TagKey<Item> tag : compiledTags) {
            if (itemStack2.is(tag)) return true;
        }

        for (var id : compiledItems) {
            if (itemStack2.is(BuiltInRegistries.ITEM.get(id))) return true;
        }

        return false;
    }

    public static class Config {
        public List<String> items = List.of();
    }
}