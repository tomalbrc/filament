package de.tomalbrc.filament.registry.filament;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class StrippableRegistry {
    private static Map<BlockState, ResourceLocation> strippables = new Reference2ObjectArrayMap<>();

    public static BlockState getStrippable(BlockState blockState) {
        return BuiltInRegistries.BLOCK.get(strippables.get(blockState)).defaultBlockState();
    }

    public static boolean has(BlockState blockState) {
        return strippables.containsKey(blockState);
    }

    public static void add(BlockState blockState, ResourceLocation replace) {
        strippables.putIfAbsent(blockState, replace);
    }
}
