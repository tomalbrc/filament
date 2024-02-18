package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.resin.data.AjLoader;
import de.tomalbrc.resin.model.AjModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;

public class AjModelRegistry {
    private static final Object2ObjectOpenHashMap<String, AjModel> ajmodels = new Object2ObjectOpenHashMap<>();

    public static AjModel getModel(String name) {
        return ajmodels.get(name);
    }

    public static class AjModelReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return new ResourceLocation("filament:ajmodel");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/ajmodel", path -> path.getPath().endsWith(".json"));

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (var inputStream = entry.getValue().open()) {
                    AjModel model = AjLoader.load(entry.getKey().toString(), inputStream);
                    ajmodels.put(entry.getKey().getPath(), model);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load decoration resource \"" + entry.getKey() + "\".");
                }
            }

            Filament.LOGGER.info("filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS);
            Filament.LOGGER.info("filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES);
        }
    }
}
