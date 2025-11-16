package de.tomalbrc.filament.registry;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class OxidizableRegistry {
    private static final BiMap<Block, ResourceLocation> oxidizables = HashBiMap.create();

    private static final Supplier<BiMap<Block, ResourceLocation>> OXIDIZABLES_NEXT = Suppliers.memoize(() -> oxidizables);
    private static final Supplier<BiMap<ResourceLocation, Block>> OXIDIZABLES_PREVIOUS = Suppliers.memoize(() -> OXIDIZABLES_NEXT.get().inverse());

    public static Block getNext(Block block) {
        return BuiltInRegistries.BLOCK.get(oxidizables.get(block));
    }

    public static Block getPrevious(Block block) {
        return OXIDIZABLES_PREVIOUS.get().get(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static boolean hasNext(Block block) {
        return OXIDIZABLES_NEXT.get().containsKey(block);
    }

    public static boolean hasPrevious(Block block) {
        return OXIDIZABLES_PREVIOUS.get().containsKey(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static void add(Block block, ResourceLocation replacement) {
        oxidizables.putIfAbsent(block, replacement);
    }

    public static boolean sameOxidizable(Block block, Block block2) {
        return hasNext(block) && getNext(block) == block2 || hasPrevious(block) && getPrevious(block) == block2;
    }
}
