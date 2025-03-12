package de.tomalbrc.filament.util;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FilamentAssetReloadListener implements FilamentSynchronousResourceReloadListener {
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
                    boolean isZip = packResources instanceof FilePackResources;
                    for (String namespace : clientResources) {
                        if (isZip) {
                            listResources((FilePackResources)packResources, PackType.CLIENT_RESOURCES, namespace, "", (resourceLocation,ioSupplier) -> {
                                try {
                                    resourcePackBuilder.addData("assets/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath(), ioSupplier.get().readAllBytes());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        abstractPackResources.listResources(PackType.CLIENT_RESOURCES, isZip ? namespace : "", !isZip ? namespace : "", (resourceLocation,ioSupplier) -> {
                            try {
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

    public void listResources(FilePackResources resources, PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        ZipFile zipFile = resources.zipFileAccess.getOrCreateZipFile();
        if (zipFile != null) {
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            String var10001 = packType.getDirectory();
            String string3 = resources.addPrefix(var10001 + "/" + string + "/");
            String string4 = string3 + string2;

            while(enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (!zipEntry.isDirectory()) {
                    String string5 = zipEntry.getName();
                    if (string5.startsWith(string4)) {
                        String string6 = string5.substring(string3.length());
                        ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string6);
                        if (resourceLocation != null) {
                            resourceOutput.accept(resourceLocation, IoSupplier.create(zipFile, zipEntry));
                        }
                    }
                }
            }
        }
    }
}