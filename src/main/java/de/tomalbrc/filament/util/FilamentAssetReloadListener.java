package de.tomalbrc.filament.util;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public class FilamentAssetReloadListener implements SimpleSynchronousResourceReloadListener {
    Consumer<ResourcePackBuilder> lastConsumer = null;

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "assets");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // ok lets just hope capturing resourceManager like this works in java™
        Consumer<ResourcePackBuilder> consumer = resourcePackBuilder -> {
            resourceManager.listPacks().forEach(resources -> {
                Set<String> clientResources = resources.getNamespaces(PackType.CLIENT_RESOURCES);
                if (resources instanceof PathPackResources pathPackResources) {
                    for (var namespace : clientResources) {
                        pathPackResources.listResources(PackType.CLIENT_RESOURCES, "", namespace, (resourceLocation,ioSupplier) -> {
                            try {
                                resourcePackBuilder.addData("assets/" + resourceLocation.getPath(), ioSupplier.get().readAllBytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            });
        };

        if (lastConsumer != null) {
            PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.unregister(lastConsumer);
        }

        lastConsumer = consumer;
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(consumer);
    }
}