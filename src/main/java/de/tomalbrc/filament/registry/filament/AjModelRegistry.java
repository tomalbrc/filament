package de.tomalbrc.filament.registry.filament;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.filament.Filament;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;

public class AjModelRegistry {
    private static final Object2ReferenceMap<ResourceLocation, Model> ajmodels = new Object2ReferenceArrayMap<>();

    public static Model getModel(ResourceLocation model) {
        return ajmodels.get(model);
    }

    public static class AjModelReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return new ResourceLocation("filament:ajmodel");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/model", path -> path.getPath().endsWith(".ajmodel"));

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (var inputStream = entry.getValue().open()) {
                    Model model = new AjModelLoader().load(inputStream, entry.getKey().getPath().toString());
                    String path = entry.getKey().getPath();
                    String customPath = path.substring(path.contains("/") ? path.lastIndexOf('/')+1 : 0, path.lastIndexOf('.'));
                    ajmodels.put(new ResourceLocation(entry.getKey().getNamespace(), customPath), model);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load decoration resource \"" + entry.getKey() + "\".");
                }
            }

            Filament.LOGGER.info("filament decorations registered: " + DecorationRegistry.REGISTERED_DECORATIONS);
            Filament.LOGGER.info("filament decoration block entities registered: " + DecorationRegistry.REGISTERED_BLOCK_ENTITIES);
        }
    }
}
