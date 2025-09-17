package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.block.SimpleBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncBlockTicker {
    private record TickData(BlockPos blockPos, SimpleBlock block, ServerLevel serverLevel) {}
    private static final Long2ObjectAVLTreeMap<TickData> TICKING = new Long2ObjectAVLTreeMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static void tick(MinecraftServer server) {
        for (TickData entry : TICKING.values()) {
            CompletableFuture.runAsync(() -> tick(entry), EXECUTOR_SERVICE);
        }
    }

    private static void tick(TickData tickData) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : tickData.block.getBehaviours()) {
            if (behaviour.getValue() instanceof AsyncTickingBlockBehaviour blockBehaviour) {
                blockBehaviour.tickAsync(tickData.serverLevel.getBlockState(tickData.blockPos), tickData.serverLevel, tickData.blockPos, tickData.serverLevel.random);
            }
        }
    }

    public static void add(BlockPos pos, SimpleBlock block, ServerLevel serverLevel) {
        TICKING.put(pos.asLong(), new TickData(pos, block, serverLevel));
    }

    public static void remove(BlockPos pos) {
        TICKING.remove(pos.asLong());
    }

    public static void remove(ServerLevel serverLevel, LevelChunk chunk) {
        LongSet s = new LongArraySet();
        for (Long2ObjectMap.Entry<TickData> entry : TICKING.long2ObjectEntrySet()) {
            if (entry.getValue().serverLevel == serverLevel && SectionPos.of(entry.getValue().blockPos).chunk().equals(chunk.getPos())) {
                s.add(entry.getLongKey());
            }
        }
        s.forEach(TICKING::remove);
    }

    public static SimpleBlock get(BlockPos pos) {
        var v = TICKING.get(pos.asLong());
        if (v != null)
            return v.block;

        return null;
    }
}
