package de.tomalbrc.filament.util;

import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.behaviour.AsyncTickingBlockBehaviour;
import de.tomalbrc.filament.block.SimpleBlock;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;
import java.util.concurrent.*;

public class AsyncBlockTicker {
    public record DataKey(String name) {}
    public record TickData(BlockPos blockPos, SimpleBlock block, ServerLevel serverLevel, Map<DataKey, Object> userData) { }

    private static final Map<Long, TickData> TICKING = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            try {
                if (!EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
                    EXECUTOR_SERVICE.shutdown();
                }
            } catch (InterruptedException ignored) {
            } finally {
                EXECUTOR_SERVICE.shutdown();
            }
        });
    }

    public static void tick(MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            for (TickData entry : TICKING.values()) {
                tick(entry);
            }
        }, EXECUTOR_SERVICE);
    }

    private static void tick(TickData tickData) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Behaviour<?>> behaviour : tickData.block.getBehaviours()) {
            if (behaviour.getValue() instanceof AsyncTickingBlockBehaviour blockBehaviour) {
                var state = tickData.serverLevel.getBlockState(tickData.blockPos);
                if (state.getBlock() == tickData.block)
                    blockBehaviour.tickAsync(tickData.serverLevel.getBlockState(tickData.blockPos), tickData.serverLevel, tickData.blockPos, tickData.serverLevel.random);
            }
        }
    }

    public static void add(BlockPos pos, SimpleBlock block, ServerLevel serverLevel) {
        TICKING.put(pos.asLong(), new TickData(pos, block, serverLevel, new Reference2ObjectOpenHashMap<>()));
    }

    public static void add(BlockPos pos, SimpleBlock block, ServerLevel serverLevel, Map<DataKey, Object> userData) {
        TICKING.put(pos.asLong(), new TickData(pos, block, serverLevel, userData));
    }

    public static void remove(BlockPos pos) {
        TICKING.remove(pos.asLong());
    }

    public static void remove(ServerLevel serverLevel, LevelChunk chunk) {
        LongSet s = new LongArraySet();
        for (Map.Entry<Long, TickData> entry : TICKING.entrySet()) {
            if (entry.getValue().serverLevel == serverLevel && SectionPos.of(entry.getValue().blockPos).chunk().equals(chunk.getPos())) {
                s.add(entry.getKey());
            }
        }
        s.forEach(TICKING::remove);
    }

    public static TickData get(BlockPos pos) {
        return TICKING.get(pos.asLong());
    }

    public static SimpleBlock getBlock(BlockPos pos) {
        var v = TICKING.get(pos.asLong());
        if (v != null)
            return v.block;

        return null;
    }
}
