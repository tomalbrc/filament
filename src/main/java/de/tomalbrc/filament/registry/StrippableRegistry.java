package de.tomalbrc.filament.registry;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class StrippableRegistry {
    private static final Map<Block, ResourceLocation> strippables = new Reference2ObjectArrayMap<>();

    public static Block get(Block block) {
        return BuiltInRegistries.BLOCK.get(strippables.get(block)).orElseThrow().value();
    }

    public static boolean has(Block block) {
        return strippables.containsKey(block);
    }

    public static void add(Block block, ResourceLocation replacement) {
        strippables.putIfAbsent(block, replacement);
    }
}
