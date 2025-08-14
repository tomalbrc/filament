package de.tomalbrc.filament;

import de.tomalbrc.filament.mixin.accessor.PolymerResourcePackAccessor;
import de.tomalbrc.filament.registry.BlockRegistry;
import de.tomalbrc.filament.registry.DecorationRegistry;
import de.tomalbrc.filament.registry.ItemRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class FilamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServerPlayConnectionEvents.INIT.register((serverGamePacketListener, server) -> {
            if (!server.isDedicatedServer() && (!ItemRegistry.ITEMS_TAGS.isEmpty() || !BlockRegistry.BLOCKS_TAGS.isEmpty() || DecorationRegistry.REGISTERED_DECORATIONS > 0)) {
                PolymerResourcePackMod.generateAndCall(server, true, message -> {}, () -> {
                    var outputPath = PolymerResourcePackUtils.getMainPath();
                    var altPath = outputPath.resolveSibling(outputPath.getFileName().toString() + "_server.zip");
                    if (altPath.toFile().exists()) {
                        PolymerResourcePackAccessor.setPath(altPath);
                    }
                    Minecraft.getInstance().reloadResourcePacks();
                });
            }
        });
    }
}
