package de.tomalbrc.filament.api.registry;

import de.tomalbrc.filament.block.*;
import de.tomalbrc.filament.data.BlockData;
import de.tomalbrc.filament.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;

import java.util.Map;

public class BlockTypeRegistry {
    private static final Map<BlockData.BlockType, Class<? extends Block>> blockTypeMap = new Object2ObjectOpenHashMap<>();

    public static void init() {
        registerBuiltin();
    }

    private static void registerBuiltin() {
        register(Constants.BlockTypes.BLOCK, SimpleBlock.class);
        register(Constants.BlockTypes.COUNT, CountBlock.class);
        register(Constants.BlockTypes.COLUMN, AxisBlock.class);
        register(Constants.BlockTypes.DIRECTIONAL, DirectionalBlock.class);
        register(Constants.BlockTypes.HORIZONTAL_DIRECTIONAL, DirectionalBlock.class);
        register(Constants.BlockTypes.POWERED_DIRECTIONAL, PoweredDirectionBlock.class);
        register(Constants.BlockTypes.POWERLEVEL, PowerlevelBlock.class);
        register(Constants.BlockTypes.SLAB, SimpleSlabBlock.class);
    }

    public static void register(BlockData.BlockType type, Class<? extends Block> clazz) {
        blockTypeMap.put(type, clazz);
    }

    public static Class<? extends Block> get(BlockData.BlockType type) {
        if (type == null)
            return blockTypeMap.get(Constants.BlockTypes.BLOCK);
        return blockTypeMap.get(type);
    }
}
