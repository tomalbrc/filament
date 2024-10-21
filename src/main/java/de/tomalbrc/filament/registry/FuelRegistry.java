package de.tomalbrc.filament.registry;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.item.Item;

import java.util.Map;

public class FuelRegistry {
    private static Map<Item, Integer> cache = new Reference2ObjectArrayMap<>();

    public static Map<Item, Integer> getCache() {
        return cache;
    }

    public static void add(Item item, Integer value) {
        cache.putIfAbsent(item, value);
    }
}
