package de.tomalbrc.filament.decoration.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VirtualCollisionTracker {
    private static final Map<Long, List<ShulkerCollisionElement>> BY_CHUNK = new ConcurrentHashMap<>();

    private VirtualCollisionTracker() {}

    public static void register(ShulkerCollisionElement element, ServerLevel level, Vec3 worldPos) {
        ChunkPos chunk = ChunkPos.containing(BlockPos.containing(worldPos));
        BY_CHUNK.computeIfAbsent(chunk.pack(), k -> new CopyOnWriteArrayList<>()).add(element);
    }

    public static void unregister(ShulkerCollisionElement element, ServerLevel level, Vec3 worldPos) {
        ChunkPos chunk = ChunkPos.containing(BlockPos.containing(worldPos));
        long k = chunk.pack();
        List<ShulkerCollisionElement> list = BY_CHUNK.get(k);
        if (list == null) return;
        list.remove(element);
        if (list.isEmpty()) BY_CHUNK.remove(k);
    }

    public static boolean hasSupportBelow(ServerPlayer player) {
        ServerLevel level = player.level();
        ChunkPos center = player.chunkPosition();

        Vec3 feet = player.position();
        AABB probe = new AABB(
                feet.x - 0.3, feet.y - 0.15, feet.z - 0.3,
                feet.x + 0.3, feet.y + 0.05, feet.z + 0.3
        );

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long k = new ChunkPos(center.x() + dx, center.z() + dz).pack();
                List<ShulkerCollisionElement> list = BY_CHUNK.get(k);
                if (list == null || list.isEmpty()) continue;

                for (ShulkerCollisionElement element : list) {
                    AABB box = element.worldBox();
                    if (box != null && box.intersects(probe)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}