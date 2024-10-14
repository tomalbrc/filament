package de.tomalbrc.filament.util;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
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
        if (lastConsumer == null) {
            Consumer<ResourcePackBuilder> consumer = resourcePackBuilder -> resourceManager.listPacks().forEach(packResources -> {
                Set<String> clientResources = packResources.getNamespaces(PackType.CLIENT_RESOURCES);
                if (packResources instanceof AbstractPackResources abstractPackResources) {
                    // using cursed hack for FilePackResource in a mixin to remove double-slashes in the path when providing an empty second string
                    boolean isZip = packResources instanceof FilePackResources;
                    for (String namespace : clientResources) {
                        abstractPackResources.listResources(PackType.CLIENT_RESOURCES, isZip ? namespace : "", !isZip ? namespace : "", (resourceLocation,ioSupplier) -> {
                            try {
                                if (isZip)
                                    resourcePackBuilder.addData("assets/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath(), ioSupplier.get().readAllBytes());
                                else
                                    // and another hack, we don't provide a namespace for normals packs... so the namespace is part of the identifiers' path
                                    resourcePackBuilder.addData("assets/" + resourceLocation.getPath(), ioSupplier.get().readAllBytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            });

            lastConsumer = consumer;
            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(consumer);
        }
    }
}