package de.tomalbrc.filament.registry;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class WaxableRegistry {
    private static final BiMap<Block, ResourceLocation> waxables = HashBiMap.create();
    private static final Supplier<BiMap<ResourceLocation, Block>> waxables_prev = Suppliers.memoize(waxables::inverse);

    public static Block getWaxed(Block block) {
        return BuiltInRegistries.BLOCK.getValue(waxables.get(block));
    }

    public static Block getPrevious(Block block) {
        return waxables_prev.get().get(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static void add(Block block, ResourceLocation replacement) {
        waxables.putIfAbsent(block, replacement);
    }
}
