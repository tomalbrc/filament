package de.tomalbrc.filament.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FilamentConfig {
    static Path CONFIG_FILE_PATH = Constants.CONFIG_DIR.resolve("filament.json");
    static FilamentConfig instance;

    public boolean emissive_shader = true;

    public boolean commands = true;
    public boolean trimArmorReplaceChainmail = false;

    public boolean preventAdventureModeDecorationInteraction = true;

    public static FilamentConfig getInstance() {
        if (instance == null) {
            if (!load()) // only save if file wasn't just created
                save(); // save since newer versions may contain new options, also removes old options
        }
        return instance;
    }
    public static boolean load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new FilamentConfig();
            try {
                if (CONFIG_FILE_PATH.toFile().createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile());
                    stream.write(Json.GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            FilamentConfig.instance = Json.GSON.fromJson(new FileReader(FilamentConfig.CONFIG_FILE_PATH.toFile()), FilamentConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static void save() {
        try {
            FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile());
            stream.write(Json.GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
