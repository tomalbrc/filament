package de.tomalbrc.filament.util;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.registry.Templates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class FilamentTemplateReloadListener implements FilamentSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "template");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        load("filament/template", null, resourceManager, (id, inputStream) -> {
            try {
                Templates.add(inputStream);
            } catch (Exception e) {
                Filament.LOGGER.error("Failed to load template \"{}\".", id, e);
            }
        });
    }
}