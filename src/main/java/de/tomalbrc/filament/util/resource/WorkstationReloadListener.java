package de.tomalbrc.filament.util.resource;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.recipe.Workstations;
import de.tomalbrc.filament.util.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;

public class WorkstationReloadListener implements FilamentSynchronousResourceReloadListener {
    @Override
    public @NonNull Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "workstation");
    }

    @Override
    public void onResourceManagerReload(@NonNull ResourceManager resourceManager) {
        load("filament/workstation", null, resourceManager, (id, inputStream) -> {
            try {
                Workstations.add(inputStream);
            } catch (Exception e) {
                Filament.LOGGER.error("Failed to load workstation \"{}\".", id, e);
            }
        });
    }
}