package de.tomalbrc.filament.registry;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.FilamentSynchronousResourceReloadListener;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ModelRegistry {
    private static final String BBMODEL_SUFFIX = ".bbmodel";
    private static final String AJMODEL_SUFFIX = ".ajmodel";
    private static final String AJBP_SUFFIX = ".ajblueprint";

    private static final Object2ReferenceMap<ResourceLocation, Model> ajmodels = new Object2ReferenceArrayMap<>();

    public static Model getModel(ResourceLocation model) {
        return ajmodels.get(model);
    }

    public static class AjModelReloadListener implements FilamentSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "model");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            var resources = resourceManager.listResources("filament/model", path -> path.getPath().endsWith(AJMODEL_SUFFIX) || path.getPath().endsWith(BBMODEL_SUFFIX) || path.getPath().endsWith(AJBP_SUFFIX));

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (var inputStream = entry.getValue().open()) {
                    Model model = entry.getKey().getPath().endsWith(AJMODEL_SUFFIX) ? new AjModelLoader().load(inputStream, entry.getKey().getPath()) : entry.getKey().getPath().endsWith(AJBP_SUFFIX) ? new AjBlueprintLoader().load(inputStream, entry.getKey().getPath()) : new BbModelLoader().load(inputStream, entry.getKey().getPath());
                    ajmodels.put(sanitize(entry.getKey()), model);
                } catch (IOException | IllegalStateException e) {
                    Filament.LOGGER.error("Failed to load decoration resource \"{}\".", entry.getKey());
                }
            }

            Filament.LOGGER.info("filament models registered: {}", ajmodels.size());
        }
    }

    public static ResourceLocation sanitize(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        String customPath = path.substring(path.contains("/") ? path.lastIndexOf('/')+1 : 0, path.lastIndexOf('.'));
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), customPath);
    }

    public static void registerAjModel(InputStream inputStream, ResourceLocation resourceLocation) throws IOException {
        ajmodels.put(sanitize(resourceLocation), new AjModelLoader().load(inputStream, resourceLocation.getPath()));
    }

    public static void registerBbModel(InputStream inputStream, ResourceLocation resourceLocation) throws IOException {
        ajmodels.put(sanitize(resourceLocation), new BbModelLoader().load(inputStream, resourceLocation.getPath()));
    }

    public static void registerAjBlueprintModel(InputStream inputStream, ResourceLocation resourceLocation) throws IOException {
        ajmodels.put(sanitize(resourceLocation), new AjBlueprintLoader().load(inputStream, resourceLocation.getPath()));
    }
}
