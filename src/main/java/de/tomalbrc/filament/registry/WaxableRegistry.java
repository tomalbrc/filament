package de.tomalbrc.filament.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class WaxableRegistry {
    private static final BiMap<Block, ResourceLocation> waxables = HashBiMap.create();

    public static Block getWaxed(Block block) {
        return BuiltInRegistries.BLOCK.getValue(waxables.get(block));
    }

    public static void add(Block block, ResourceLocation replacement) {
        waxables.putIfAbsent(block, replacement);
    }
}
