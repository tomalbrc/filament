package de.tomalbrc.filament.registry;

import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.config.data.DecorationData;
import de.tomalbrc.filament.decoration.DecorationItem;
import de.tomalbrc.filament.util.Constants;
import de.tomalbrc.filament.util.Json;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.provim.nylon.data.AjLoader;
import org.provim.nylon.model.AjModel;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;

public class DecorationRegistry {
    public static final File DIR = Constants.CONFIG_DIR.resolve("decoration").toFile();

    private static final Object2ObjectOpenHashMap<ResourceLocation, DecorationData> decorations = new Object2ObjectOpenHashMap<>();

    private static final Object2ObjectOpenHashMap<String, AjModel> ajmodels = new Object2ObjectOpenHashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        if (!DIR.exists() || !DIR.isDirectory()) {
            DIR.mkdirs();
            return;
        }

        preloadModels();

        Collection<File> files = FileUtils.listFiles(DIR, new String[]{"json"}, true);
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    DecorationData data = Json.GSON.fromJson(reader, DecorationData.class);
                    decorations.put(data.id(), data);
                    ItemRegistry.registerItem(data.id(), new DecorationItem(data), ItemRegistry.CUSTOM_DECORATIONS);
                } catch (Throwable throwable) {
                    Filament.LOGGER.error("Error reading decoration JSON file: {}", file.getAbsolutePath(), throwable);
                }
            }
        }
    }

    public static DecorationData getDecorationDefinition(ResourceLocation resourceLocation) {
        return decorations.get(resourceLocation);
    }

    public static AjModel getModel(String name) {
        return ajmodels.get(name);
    }

    private static void preloadModels() {
        String path = String.format("%s/ajmodel/", Constants.CONFIG_DIR);
        File modelDir = new File(path);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            modelDir.mkdirs();
            return;
        }

        Collection<File> files = FileUtils.listFiles(modelDir, new String[]{"json"}, true);

        if (files != null) {
            for (File file : files) {
                AjModel model = AjLoader.require(file.getPath());
                ajmodels.put(file.getName(), model);
            }
        }
    }
}
