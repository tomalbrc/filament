package de.tomalbrc.filament.util.resource;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.registry.Templates;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;

public class FilamentTemplateReloadListener implements FilamentSynchronousResourceReloadListener {
    @Override
    public @NonNull Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "template");
    }

    @Override
    public void onResourceManagerReload(@NonNull ResourceManager resourceManager) {
        load("filament/template", null, resourceManager, (id, inputStream) -> {
            try {
                Templates.add(inputStream);
            } catch (Exception e) {
                Filament.LOGGER.error("Failed to load template \"{}\".", id, e);
            }
        });
    }
}