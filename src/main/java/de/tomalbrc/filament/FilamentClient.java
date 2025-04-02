package de.tomalbrc.filament;

import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public class FilamentClient {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, sender, server) -> {
            if (!server.isDedicatedServer() && (!ItemRegistry.ITEMS_TAGS.isEmpty() || !BlockRegistry.BLOCKS_TAGS.isEmpty() || DecorationRegistry.REGISTERED_DECORATIONS > 0)) {
                PolymerResourcePackMod.generateAndCall(server, true, message -> {}, () -> Minecraft.getInstance().reloadResourcePacks());
            }
        });
    }
}
