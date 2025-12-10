package de.tomalbrc.filament.registry;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class StrippableRegistry {
    private static final Map<Block, Pair<Identifier, Identifier>> strippables = new Reference2ObjectArrayMap<>();

    public static Block get(Block block) {
        if (!has(block))
            return null;

        return BuiltInRegistries.BLOCK.getValue(strippables.get(block).getFirst());
    }

    public static Identifier getLootTable(Block block) {
        return strippables.get(block).getSecond();
    }

    public static boolean has(Block block) {
        return strippables.containsKey(block);
    }

    public static void add(Block block, Identifier replacement, Identifier lootTable) {
        strippables.putIfAbsent(block, Pair.of(replacement, lootTable));
    }
}
